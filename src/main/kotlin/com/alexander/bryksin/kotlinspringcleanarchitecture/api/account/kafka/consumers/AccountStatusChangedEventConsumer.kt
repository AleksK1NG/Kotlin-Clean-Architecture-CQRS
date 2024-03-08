package com.alexander.bryksin.kotlinspringcleanarchitecture.api.account.kafka.consumers

import com.alexander.bryksin.kotlinspringcleanarchitecture.api.account.kafka.processor.EventProcessor
import com.alexander.bryksin.kotlinspringcleanarchitecture.api.configuration.kafka.KafkaTopics
import com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.events.AccountStatusChangedEvent
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component


@Component
class AccountStatusChangedEventConsumer(
    private val eventProcessor: EventProcessor,
    private val kafkaTopics: KafkaTopics,
) {

    @KafkaListener(
        groupId = "\${kafka.consumer-group-id:account_microservice_group_id}",
        topics = ["\${topics.accountStatusChanged.name}"],
    )
    fun process(ack: Acknowledgment, record: ConsumerRecord<String, ByteArray>) = eventProcessor.process(
        ack = ack,
        consumerRecord = record,
        deserializationClazz = AccountStatusChangedEvent::class.java,
        onError = eventProcessor.retryHandler(kafkaTopics.accountStatusChangedRetry.name, DEFAULT_RETRY_COUNT)
    ) { event ->
        eventProcessor.on(
            ack = ack,
            consumerRecord = record,
            event = event,
            retryTopic = kafkaTopics.accountStatusChangedRetry.name
        )
    }


    @KafkaListener(
        groupId = "\${kafka.consumer-group-id:account_microservice_group_id}",
        topics = ["\${topics.accountStatusChangedRetry.name}"],
    )
    fun processRetry(ack: Acknowledgment, record: ConsumerRecord<String, ByteArray>) = eventProcessor.process(
        ack = ack,
        consumerRecord = record,
        deserializationClazz = AccountStatusChangedEvent::class.java,
        onError = eventProcessor.retryHandler(kafkaTopics.accountStatusChangedRetry.name, DEFAULT_RETRY_COUNT)
    ) { event ->
        eventProcessor.on(
            ack = ack,
            consumerRecord = record,
            event = event,
            retryTopic = kafkaTopics.accountStatusChangedRetry.name
        )
    }

    private companion object {
        private const val DEFAULT_RETRY_COUNT = 3
    }
}