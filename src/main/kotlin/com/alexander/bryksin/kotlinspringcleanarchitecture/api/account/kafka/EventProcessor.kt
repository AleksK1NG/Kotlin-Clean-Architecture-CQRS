package com.alexander.bryksin.kotlinspringcleanarchitecture.api.account.kafka

import com.alexander.bryksin.kotlinspringcleanarchitecture.api.common.kafkaUtils.*
import com.alexander.bryksin.kotlinspringcleanarchitecture.api.configuration.kafka.KafkaTopics
import com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.exceptions.LowerEventVersionException
import com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.exceptions.SameEventVersionException
import com.alexander.bryksin.kotlinspringcleanarchitecture.application.common.publisher.EventPublisher
import com.alexander.bryksin.kotlinspringcleanarchitecture.application.common.serializer.SerializationException
import com.alexander.bryksin.kotlinspringcleanarchitecture.application.common.serializer.Serializer
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.exceptions.InvalidCurrencyException
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.Job
import kotlinx.coroutines.runBlocking
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.dao.DuplicateKeyException
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext


typealias ErrorHandler <T> = suspend (Throwable, Acknowledgment, ConsumerRecord<String, ByteArray>, Class<T>) -> Unit

typealias OnErrorHandler <T> = suspend (ErrorHandlerParams<T>) -> Unit


data class ErrorHandlerParams<T>(
    val error: Throwable,
    val ack: Acknowledgment,
    val consumerRecord: ConsumerRecord<String, ByteArray>,
    val deserializationClazz: Class<T>
)

@Component
class EventProcessor(
    private val serializer: Serializer,
    private val publisher: EventPublisher,
    private val kafkaTopics: KafkaTopics
) {

    fun <T> process(
        ack: Acknowledgment,
        consumerRecord: ConsumerRecord<String, ByteArray>,
        deserializationClazz: Class<T>,
        unprocessableExceptions: Set<Class<*>> = setOf(),
        retryTopic: String? = null,
        context: CoroutineContext = EmptyCoroutineContext,
        onError: OnErrorHandler<T> = { log.error { it.error.message } },
        onSuccess: suspend (T) -> Unit
    ) = runBlocking(processContext(context)) {
        try {
            log.info { consumerRecord.info() }
            val event = serializer.deserialize(consumerRecord.value(), deserializationClazz)
            onSuccess(event)
        } catch (e: Exception) {
            log.error { "error while processing event: ${e.message}, data: ${consumerRecord.info()}" }

            if (unprocessableExceptions.contains(e::class.java) || dlqExceptions.contains(e::class.java)) {
                log.warn { "publishing to DLQ: ${e.message}, data: ${consumerRecord.info()}" }

                publisher.publish(
                    topic = kafkaTopics.deadLetterQueue.name,
                    key = consumerRecord.key(),
                    data = consumerRecord.value(),
                    headers = consumerRecord.headersToMap()
                )

                ack.acknowledge()
                return@runBlocking
            }

            val errorHandlerParams = ErrorHandlerParams(
                error = e,
                ack = ack,
                consumerRecord = consumerRecord,
                deserializationClazz = deserializationClazz
            )

            onError(errorHandlerParams)
        }
    }

    fun <T : Any> defaultErrorRetryHandler(
        retryTopic: String,
        maxRetryCount: Int = DEFAULT_RETRY_COUNT,
    ): OnErrorHandler<T> =
        {
            handleRetry(
                err = it.error,
                ack = it.ack,
                consumerRecord = it.consumerRecord,
                maxRetryCount = maxRetryCount,
                retryTopic = retryTopic,
                deserializationClazz = it.deserializationClazz
            )
        }

    private suspend fun <T : Any> handleRetry(
        err: Throwable,
        ack: Acknowledgment,
        consumerRecord: ConsumerRecord<String, ByteArray>,
        maxRetryCount: Int,
        retryTopic: String,
        deserializationClazz: Class<T>
    ) {
        val event = runCatching<T> { serializer.deserialize(consumerRecord.value(), deserializationClazz) }
            .onFailure { log.error { "serialization error: ${it.message}" } }
            .onSuccess { log.info { "serialized message: $it" } }
            .getOrThrow()

        log.error { "error while processing record: ${consumerRecord.info(withValue = false)}, error: ${err.message}" }

        val retryCount = consumerRecord.getRetriesCount().getOrDefault(0)
        val retryHeadersMap = buildRetryCountHeader(retryCount + 1)
        log.info { "retry count: $retryCount - map: $${String(retryHeadersMap[KAFKA_HEADERS_RETRY] ?: byteArrayOf())}" }

        val mergedHeaders = consumerRecord.mergeHeaders(retryHeadersMap)
        log.info { "merged headers retry: ${String(mergedHeaders[KAFKA_HEADERS_RETRY] ?: byteArrayOf())}" }

        if (retryCount >= maxRetryCount) {
            publisher.publishBytes(
                topic = kafkaTopics.deadLetterQueue.name,
                key = consumerRecord.key(),
                data = consumerRecord.value(),
                headers = retryHeadersMap
            )
            logDlqMsg(retryCount, maxRetryCount, consumerRecord)
            ack.acknowledge()
            return
        }

        publisher.publish(
            topic = retryTopic,
            key = consumerRecord.key(),
            data = event,
            headers = mergedHeaders
        )

        log.warn { "published retry topic: $retryTopic, event: $event" }
        ack.acknowledge()
    }

    fun processContext(context: CoroutineContext = EmptyCoroutineContext): CoroutineContext =
        Job() + CoroutineName(this::class.java.name) + context

    private fun logDlqMsg(
        retryCount: Int,
        maxRetryCount: Int,
        consumerRecord: ConsumerRecord<String, ByteArray>
    ) {
        log.error {
            "retry: $retryCount of $maxRetryCount exceed, published to dlq: ${kafkaTopics.deadLetterQueue.name} data: ${consumerRecord.info()}"
        }
    }

    companion object {
        private val log = KotlinLogging.logger { }
        private val dlqExceptions = setOf(
            SerializationException::class.java,
            LowerEventVersionException::eventVersion,
            SameEventVersionException::class.java,
            DuplicateKeyException::class.java,
            InvalidCurrencyException::class
        )
        const val KAFKA_HEADERS_RETRY = "X-Kafka-Retry"
        private const val DEFAULT_RETRY_COUNT = 3
    }
}

