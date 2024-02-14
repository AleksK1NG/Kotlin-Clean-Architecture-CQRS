package com.alexander.bryksin.kotlinspringcleanarchitecture.api.account.kafka

import com.alexander.bryksin.kotlinspringcleanarchitecture.api.common.deserializeRecordToEvent
import com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.events.AccountStatusChangedEvent
import com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.services.AccountEventsHandler
import com.alexander.bryksin.kotlinspringcleanarchitecture.application.common.publisher.EventPublisher
import com.alexander.bryksin.kotlinspringcleanarchitecture.application.common.serializer.SerializationException
import com.alexander.bryksin.kotlinspringcleanarchitecture.application.common.serializer.Serializer
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.runBlocking
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component


@Component
class AccountStatusChangedEventConsumer(
    private val serializer: Serializer,
    private val accountEventsHandler: AccountEventsHandler,
    private val eventPublisher: EventPublisher,
) {

    @KafkaListener(
        groupId = "\${kafka.consumer-group-id:account_microservice_group_id}",
        topics = ["\${topics.accountStatusChanged.name}"],
    )
    fun process(ack: Acknowledgment, record: ConsumerRecord<String, ByteArray>) = runBlocking {
        try {
            log.info { "processing ${record.topic()} value: ${String(record.value())}" }
            val event = serializer.deserializeRecordToEvent(record, AccountStatusChangedEvent::class.java)
            accountEventsHandler.on(event)
            ack.acknowledge()
        } catch (e: Exception) {
            log.error { "error while processing event: ${e.message}" }
            if (unprocessableExceptions.contains(e::class.java)) {
                log.warn { "publishing to DLQ: ${e.message}" }
//                eventPublisher.publish()
                ack.acknowledge()
                return@runBlocking
            }
        }
    }


    private companion object {
        private val log = KotlinLogging.logger { }
        private val unprocessableExceptions = setOf(SerializationException::class.java)
    }
}