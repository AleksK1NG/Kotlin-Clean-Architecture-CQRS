package com.alexander.bryksin.kotlinspringcleanarchitecture.api.account.kafka

import arrow.core.Either
import com.alexander.bryksin.kotlinspringcleanarchitecture.api.common.kafkaUtils.*
import com.alexander.bryksin.kotlinspringcleanarchitecture.api.configuration.kafka.KafkaTopics
import com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.events.*
import com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.exceptions.LowerEventVersionException
import com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.exceptions.SameEventVersionException
import com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.persistance.AccountProjectionRepository
import com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.queries.GetAccountByIdQuery
import com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.services.AccountEventHandlerService
import com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.services.AccountQueryService
import com.alexander.bryksin.kotlinspringcleanarchitecture.application.common.publisher.EventPublisher
import com.alexander.bryksin.kotlinspringcleanarchitecture.application.common.serializer.SerializationException
import com.alexander.bryksin.kotlinspringcleanarchitecture.application.common.serializer.Serializer
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.errors.*
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.exceptions.InvalidCurrencyException
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.valueObjects.AccountId
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.common.scope.eitherScope
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
    private val kafkaTopics: KafkaTopics,
    private val accountEventHandlerService: AccountEventHandlerService,
    private val accountQueryService: AccountQueryService,
    private val accountProjectionRepository: AccountProjectionRepository
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

            if (isUnprocessableException(e, unprocessableExceptions)) {
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

    fun <T : Any> errorRetryHandler(
        retryTopic: String,
        maxRetryCount: Int = DEFAULT_RETRY_COUNT,
    ): OnErrorHandler<T> =
        {
            handleRetry(
                errorHandlerParams = it,
                maxRetryCount = maxRetryCount,
                retryTopic = retryTopic,
            )
        }

    private suspend fun <T : Any> handleRetry(
        errorHandlerParams: ErrorHandlerParams<T>,
        maxRetryCount: Int,
        retryTopic: String,
    ) {
        val (err, ack, consumerRecord, deserializationClazz) = errorHandlerParams
        log.error { "error while processing record: ${consumerRecord.info(withValue = false)}, error: ${err.message}" }

        val event = runCatching<T> { serializer.deserialize(consumerRecord.value(), deserializationClazz) }
            .onFailure { log.error { "serialization error: ${it.message}" } }
            .onSuccess { log.info { "serialized message: $it" } }
            .getOrThrow()

        val retryCount = consumerRecord.getRetriesCount().getOrDefault(BASE_RETRY_COUNT)
        val retryHeadersMap = buildRetryCountHeader(retryCount + RETRY_COUNT_STEP)
        log.info { "retry count: $retryCount - map: $${String(retryHeadersMap[KAFKA_HEADERS_RETRY] ?: byteArrayOf())}" }

        if (retryCount >= maxRetryCount) {
            val dlqHeaders = retryHeadersMap.toMutableMap()
            dlqHeaders[DLQ_ERROR_MESSAGE] = err.message?.toByteArray(Charsets.UTF_8) ?: byteArrayOf()

            publisher.publishBytes(
                topic = kafkaTopics.deadLetterQueue.name,
                key = consumerRecord.key(),
                data = consumerRecord.value(),
                headers = dlqHeaders
            )

            logDlqMsg(retryCount, maxRetryCount, consumerRecord)
            ack.acknowledge()
            restoreProjection(event)
            return
        }

        val mergedHeaders = consumerRecord.mergeHeaders(retryHeadersMap)
        log.info { "merged headers retry: ${String(mergedHeaders[KAFKA_HEADERS_RETRY] ?: byteArrayOf())}" }

        publisher.publish(
            topic = retryTopic,
            key = consumerRecord.key(),
            data = event,
            headers = mergedHeaders
        )

        log.warn { "published retry topic: $retryTopic, event: $event" }
        ack.acknowledge()
    }


    internal suspend fun <T : DomainEvent> on(
        ack: Acknowledgment,
        consumerRecord: ConsumerRecord<String, ByteArray>,
        event: T,
        retryTopic: String,
        maxRetryCount: Int = DEFAULT_RETRY_COUNT,
    ) {
        handle(event).fold(
            ifLeft = { err ->
                if (unProcessableDomainErrors.contains(err::class.java) || consumerRecord.retryCount() >= maxRetryCount) {
                    log.error { "unprocessable domain error: $err" }

                    publisher.publish(
                        topic = kafkaTopics.deadLetterQueue.name,
                        key = event.aggregateId,
                        data = event,
                        headers = consumerRecord.getKafkaRetryHeaders(err = err)
                    )

                    ack.acknowledge()
                    restoreProjection(event)
                    return@fold
                }

                publisher.publish(
                    topic = retryTopic,
                    key = event.aggregateId,
                    data = event,
                    headers = consumerRecord.getKafkaRetryHeaders(err = err)
                )

                log.warn { "published to retry: ${consumerRecord.info(withValue = true)}" }
                ack.acknowledge()
            },

            ifRight = {
                log.info { "record successfully processed: ${consumerRecord.info()}" }
                ack.acknowledge()
            }
        )
    }

    internal suspend fun handle(event: DomainEvent): Either<AppError, Unit> {
        return when (event) {
            is AccountCreatedEvent -> accountEventHandlerService.on(event)
            is AccountStatusChangedEvent -> accountEventHandlerService.on(event)
            is BalanceDepositedEvent -> accountEventHandlerService.on(event)
            is BalanceWithdrawEvent -> accountEventHandlerService.on(event)
            is ContactInfoChangedEvent -> accountEventHandlerService.on(event)
            is PersonalInfoUpdatedEvent -> accountEventHandlerService.on(event)
        }
    }


    internal suspend fun restoreProjection(event: Any) = eitherScope {
        if (event !is DomainEvent) return@eitherScope

        val accountId = AccountId(event.aggregateId.toUUID())
        val account = accountQueryService.handle(GetAccountByIdQuery(accountId)).bind()
        val savedAccount = accountProjectionRepository.upsert(account).bind()
        log.info { "restored account: $savedAccount" }
    }.fold(
        ifLeft = { log.error { "error while trying restore projection" } },
        ifRight = { log.info { "projection restored for event: $event" } }
    )

    private fun processContext(context: CoroutineContext = EmptyCoroutineContext): CoroutineContext =
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

        val dlqExceptions = setOf(
            SerializationException::class.java,
            LowerEventVersionException::eventVersion,
            SameEventVersionException::class.java,
            DuplicateKeyException::class.java,
            InvalidCurrencyException::class
        )

        val unProcessableDomainErrors = setOf(
            EmailValidationError::class.java,
            InvalidBalanceError::class.java,
            PaymentValidationError::class.java,
            LowerEventVersionError::class.java,
            SameEventVersionError::class.java,
        )

        const val KAFKA_HEADERS_RETRY = "X-Kafka-Retry"
        const val KAFKA_HEADERS_ERROR_MESSAGE = "error_message"

        private const val DEFAULT_RETRY_COUNT = 3
        private const val DLQ_ERROR_MESSAGE = "dlqErrorMessage"
        private const val BASE_RETRY_COUNT = 0
        private const val RETRY_COUNT_STEP = 1
    }
}


internal fun isUnprocessableException(e: Exception, unprocessableExceptions: Set<Class<*>> = setOf()): Boolean {
    return (unprocessableExceptions.contains(e::class.java) || EventProcessor.dlqExceptions.contains(e::class.java))
}