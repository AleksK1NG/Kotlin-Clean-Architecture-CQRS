package com.alexander.bryksin.kotlinspringcleanarchitecture.api.account.kafka

import com.alexander.bryksin.kotlinspringcleanarchitecture.api.configuration.kafka.KafkaTopics
import com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.events.AccountCreatedEvent
import com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.services.AccountEventHandlerService
import com.alexander.bryksin.kotlinspringcleanarchitecture.application.common.publisher.EventPublisher
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
        onError = eventProcessor.errorRetryHandler(kafkaTopics.accountCreatedRetry.name, DEFAULT_RETRY_COUNT)
    ) { event ->
        eventProcessor.on(
            ack = ack,
            consumerRecord = record,
            event = event,
            retryTopic = kafkaTopics.accountCreatedRetry.name
        )
    }

    @KafkaListener(
        groupId = "\${kafka.consumer-group-id:account_microservice_group_id}",
        topics = ["\${topics.accountCreatedRetry.name}"],
    )
    fun processRetry(ack: Acknowledgment, record: ConsumerRecord<String, ByteArray>) = eventProcessor.process(
        ack = ack,
        consumerRecord = record,
        deserializationClazz = AccountCreatedEvent::class.java,
        onError = eventProcessor.errorRetryHandler(kafkaTopics.accountCreatedRetry.name, DEFAULT_RETRY_COUNT)
    ) { event ->
        eventProcessor.on(
            ack = ack,
            consumerRecord = record,
            event = event,
            retryTopic = kafkaTopics.accountCreatedRetry.name
        )
    }

    private companion object {
        private val log = KotlinLogging.logger { }
        private const val DEFAULT_RETRY_COUNT = 3
    }
}

