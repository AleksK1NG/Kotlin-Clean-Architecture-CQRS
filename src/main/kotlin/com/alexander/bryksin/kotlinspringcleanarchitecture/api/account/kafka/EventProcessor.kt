package com.alexander.bryksin.kotlinspringcleanarchitecture.api.account.kafka

import com.alexander.bryksin.kotlinspringcleanarchitecture.api.common.deserializeRecordToEvent
import com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.exceptions.LowerEventVersionException
import com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.exceptions.SameEventVersionException
import com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.services.AccountEventsHandler
import com.alexander.bryksin.kotlinspringcleanarchitecture.application.common.publisher.EventPublisher
import com.alexander.bryksin.kotlinspringcleanarchitecture.application.common.serializer.SerializationException
import com.alexander.bryksin.kotlinspringcleanarchitecture.application.common.serializer.Serializer
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.runBlocking
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component

@Component
class EventProcessor(
    private val serializer: Serializer,
    private val accountEventsHandler: AccountEventsHandler,
    private val eventPublisher: EventPublisher,
    private val kafkaTemplate: KafkaTemplate<String, ByteArray>,
) {

    fun <T> runProcess(
        ack: Acknowledgment,
        consumerRecord: ConsumerRecord<String, ByteArray>,
        deserializationClazz: Class<T>,
        unprocessableExceptions: Set<Class<*>> = setOf(),
        onError: (Throwable) -> Unit,
        onSuccess: suspend (T) -> Unit
    ) = runBlocking {
        try {
            log.info { "processing ${consumerRecord.topic()} value: ${String(consumerRecord.value())}" }
            val event = serializer.deserializeRecordToEvent(consumerRecord, deserializationClazz)
            onSuccess(event)
        } catch (e: Exception) {
            log.error { "error while processing event: ${e.message}" }

            if (unprocessableExceptions.contains(e::class.java) || dlqExceptions.contains(e::class.java)) {
                log.warn { "publishing to DLQ: ${e.message}" }
                // publish to dlq
                ack.acknowledge()
                return@runBlocking
            }

            onError(e)
        }
    }

    private companion object {
        private val log = KotlinLogging.logger { }
        private val dlqExceptions = setOf(
            SerializationException::class.java,
            LowerEventVersionException::eventVersion,
            SameEventVersionException::class.java
        )
    }
}