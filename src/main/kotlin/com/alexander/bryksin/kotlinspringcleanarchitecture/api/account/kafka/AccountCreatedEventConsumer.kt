package com.alexander.bryksin.kotlinspringcleanarchitecture.api.account.kafka

import com.alexander.bryksin.kotlinspringcleanarchitecture.api.common.kafkaUtils.buildRetryCountHeader
import com.alexander.bryksin.kotlinspringcleanarchitecture.api.common.kafkaUtils.getRetryCount
import com.alexander.bryksin.kotlinspringcleanarchitecture.api.common.kafkaUtils.mergeHeaders
import com.alexander.bryksin.kotlinspringcleanarchitecture.api.configuration.kafka.KafkaTopics
import com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.events.AccountCreatedEvent
import com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.services.AccountEventHandlerService
import com.alexander.bryksin.kotlinspringcleanarchitecture.application.common.publisher.EventPublisher
import com.alexander.bryksin.kotlinspringcleanarchitecture.application.common.serializer.SerializationException
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
        onError = eventProcessor.defaultErrorRetryHandler(kafkaTopics.accountCreatedRetry.name, 3)
    ) { event ->
        accountEventHandlerService.on(event)
        ack.acknowledge()
        log.info { "consumerRecord successfully processed: $record" }
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
        onError = eventProcessor.defaultErrorRetryHandler(kafkaTopics.accountCreatedRetry.name, 3)
    ) { event ->
        accountEventHandlerService.on(event)
        ack.acknowledge()
        log.info { "consumerRecord successfully processed: $record" }
    }

    private val accountCreatedErrorHandler: ErrorHandler<AccountCreatedEvent> = { err, ack, consumerRecord, clazz ->
        log.error { "error while processing record: ${consumerRecord.topic()} key:${consumerRecord.key()}, error: ${err.message}" }

        val retryCount = consumerRecord.getRetryCount()
        val retryHeadersMap = buildRetryCountHeader(retryCount + 1)
        val mergedHeaders = consumerRecord.mergeHeaders(retryHeadersMap)

        publisher.publish(
            topic = kafkaTopics.accountCreatedRetry.name,
            key = consumerRecord.key(),
            data = consumerRecord.value(),
            headers = mergedHeaders
        )

        log.warn { "published retry topic: ${kafkaTopics.accountCreated.name}" }
        ack.acknowledge()
    }


    private companion object {
        private val log = KotlinLogging.logger { }
        private val unprocessableExceptions = setOf(SerializationException::class.java)
    }
}

