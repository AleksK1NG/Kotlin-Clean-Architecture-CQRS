package com.alexander.bryksin.kotlinspringcleanarchitecture.api.account.kafka

import com.alexander.bryksin.kotlinspringcleanarchitecture.api.common.deserializeRecordToEvent
import com.alexander.bryksin.kotlinspringcleanarchitecture.api.common.kafkaUtils.*
import com.alexander.bryksin.kotlinspringcleanarchitecture.api.configuration.kafka.KafkaTopics
import com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.exceptions.LowerEventVersionException
import com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.exceptions.SameEventVersionException
import com.alexander.bryksin.kotlinspringcleanarchitecture.application.common.publisher.EventPublisher
import com.alexander.bryksin.kotlinspringcleanarchitecture.application.common.serializer.SerializationException
import com.alexander.bryksin.kotlinspringcleanarchitecture.application.common.serializer.Serializer
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.Job
import kotlinx.coroutines.runBlocking
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext


typealias ErrorHandler = suspend (Throwable, Acknowledgment, ConsumerRecord<String, ByteArray>) -> Unit

@Component
class EventProcessor(
    private val serializer: Serializer,
    private val publisher: EventPublisher,
    private val kafkaTopics: KafkaTopics
) {

    fun <T> runProcess(
        ack: Acknowledgment,
        consumerRecord: ConsumerRecord<String, ByteArray>,
        deserializationClazz: Class<T>,
        unprocessableExceptions: Set<Class<*>> = setOf(),
        retryTopic: String? = null,
        context: CoroutineContext = EmptyCoroutineContext,
        onError: ErrorHandler = { err, _, _ -> log.error { err.message } },
        onSuccess: suspend (T) -> Unit
    ) = runBlocking(processContext(context)) {
        try {
            log.info { getConsumerRecordInfo(consumerRecord) }
            val event = serializer.deserializeRecordToEvent(consumerRecord, deserializationClazz)
            onSuccess(event)
        } catch (e: Exception) {
            log.error { "error while processing event: ${e.message}" }

            if (unprocessableExceptions.contains(e::class.java) || dlqExceptions.contains(e::class.java)) {
                log.warn { "publishing to DLQ: ${e.message}" }

                publisher.publish(
                    topic = kafkaTopics.deadLetterQueue.name,
                    key = consumerRecord.key(),
                    data = consumerRecord.value(),
                    headers = consumerRecord.headersToMap()
                )

                ack.acknowledge()
                return@runBlocking
            }

            onError(e, ack, consumerRecord)
        }
    }

    fun defaultErrorRetryHandler(retryTopic: String, maxRetryCount: Int = 3): ErrorHandler =
        { err, ack, consumerRecord ->
            log.error { "error while processing record: ${consumerRecord.topic()} key:${consumerRecord.key()}, error: ${err.message}" }

            val retryCount = consumerRecord.getRetryCount()
            val retryHeadersMap = buildRetryCountHeader(retryCount + 1)
            val mergedHeaders = consumerRecord.mergeHeaders(retryHeadersMap)

            if (retryCount >= maxRetryCount) {
                publisher.publish(
                    topic = kafkaTopics.deadLetterQueue.name,
                    key = consumerRecord.key(),
                    data = consumerRecord.value(),
                    headers = mergedHeaders
                )
                log.error {
                    "retry count: $retryCount of: $maxRetryCount exceed, published to dlq: ${getConsumerRecordInfo(consumerRecord)}"
                }
                ack.acknowledge()
            } else {
                publisher.publish(
                    topic = retryTopic,
                    key = consumerRecord.key(),
                    data = consumerRecord.value(),
                    headers = mergedHeaders
                )

                log.warn { "published retry topic: ${kafkaTopics.accountCreated.name}" }
                ack.acknowledge()
            }
        }

    private fun processContext(context: CoroutineContext = EmptyCoroutineContext): CoroutineContext =
        Job() + CoroutineName(this::class.java.name) + context

    companion object {
        private val log = KotlinLogging.logger { }
        private val dlqExceptions = setOf(
            SerializationException::class.java,
            LowerEventVersionException::eventVersion,
            SameEventVersionException::class.java
        )
        const val KAFKA_HEADERS_RETRY = "X-Kafka-Retry"
    }
}