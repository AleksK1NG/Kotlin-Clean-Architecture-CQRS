package com.alexander.bryksin.kotlinspringcleanarchitecture.api.account.kafka

import com.alexander.bryksin.kotlinspringcleanarchitecture.api.common.kafka.EventProcessor
import com.alexander.bryksin.kotlinspringcleanarchitecture.api.common.kafkaUtils.info
import com.alexander.bryksin.kotlinspringcleanarchitecture.api.configuration.kafka.KafkaTopics
import com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.events.AccountCreatedEvent
import com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.exceptions.LowerEventVersionException
import com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.exceptions.SameEventVersionException
import com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.exceptions.UpperEventVersionException
import com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.services.AccountEventHandlerService
import com.alexander.bryksin.kotlinspringcleanarchitecture.application.common.publisher.EventPublisher
import com.alexander.bryksin.kotlinspringcleanarchitecture.application.common.serializer.SerializationException
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.errors.*
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.common.scope.eitherScope
import io.github.oshai.kotlinlogging.KotlinLogging
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component


@Component
class AccountCreatedEventConsumer(
    private val eventProcessor: EventProcessor,
    private val accountEventHandlerService: AccountEventHandlerService,
    private val publisher: EventPublisher,
    private val kafkaTopics: KafkaTopics
) {

    @KafkaListener(
        groupId = "\${kafka.consumer-group-id:account_microservice_group_id}",
        topics = ["\${topics.accountCreated.name}"],
    )
    fun process(ack: Acknowledgment, record: ConsumerRecord<String, ByteArray>) = eventProcessor.process(
        ack = ack,
        consumerRecord = record,
        deserializationClazz = AccountCreatedEvent::class.java,
        unprocessableExceptions = unprocessableExceptions,
        onError = eventProcessor.errorRetryHandler(kafkaTopics.accountCreatedRetry.name, DEFAULT_RETRY_COUNT)
    ) { event ->
        accountEventHandlerService.on(event)
        ack.acknowledge()
        log.info { "consumerRecord successfully processed: ${record.info(withValue = true)}" }
    }


    @KafkaListener(
        groupId = "\${kafka.consumer-group-id:account_microservice_group_id}",
        topics = ["\${topics.accountCreatedRetry.name}"],
    )
    fun processRetry(ack: Acknowledgment, record: ConsumerRecord<String, ByteArray>) = eventProcessor.process(
        ack = ack,
        consumerRecord = record,
        deserializationClazz = AccountCreatedEvent::class.java,
        unprocessableExceptions = unprocessableExceptions,
        onError = eventProcessor.errorRetryHandler(kafkaTopics.accountCreatedRetry.name, DEFAULT_RETRY_COUNT)
    ) { event ->
        eitherScope<AppError, Unit> {
            accountEventHandlerService.on(event).fold(
                ifLeft = { err ->
                    when (err) {
                        is AccountNotFoundError -> TODO()
                        is EmailValidationError -> TODO()
                        is GenericAppError -> TODO()
                        is InvalidBalanceError -> TODO()
                        is InvalidVersion -> TODO()
                        is LowerEventVersionError -> throw LowerEventVersionException(
                            err.id,
                            err.eventVersion,
                            err.expectedVersion
                        )

                        is PaymentValidationError -> TODO()
                        is SameEventVersionError -> throw SameEventVersionException(
                            err.id,
                            err.eventVersion,
                            err.expectedVersion
                        )

                        is UpperEventVersionError -> throw UpperEventVersionException(
                            err.id,
                            err.eventVersion,
                            err.expectedVersion
                        )
                    }
                },
                ifRight = { log.info { "processed" } }
            )

            ack.acknowledge()
            log.info { "consumerRecord successfully processed: $record" }
        }
    }


    private companion object {
        private val log = KotlinLogging.logger { }
        private val unprocessableExceptions = setOf(SerializationException::class.java)
        private const val DEFAULT_RETRY_COUNT = 3
    }
}

