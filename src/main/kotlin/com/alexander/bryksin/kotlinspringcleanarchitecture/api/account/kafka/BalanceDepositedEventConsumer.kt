package com.alexander.bryksin.kotlinspringcleanarchitecture.api.account.kafka

import com.alexander.bryksin.kotlinspringcleanarchitecture.api.common.kafka.EventProcessor
import com.alexander.bryksin.kotlinspringcleanarchitecture.api.configuration.kafka.KafkaTopics
import com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.events.BalanceDepositedEvent
import com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.services.AccountEventHandlerService
import io.github.oshai.kotlinlogging.KotlinLogging
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component

@Component
class BalanceDepositedEventConsumer(
    private val eventProcessor: EventProcessor,
    private val accountEventHandlerService: AccountEventHandlerService,
    private val kafkaTopics: KafkaTopics
) {


    @KafkaListener(
        groupId = "\${kafka.consumer-group-id:account_microservice_group_id}",
        topics = ["\${topics.accountBalanceDeposited.name}"],
    )
    fun process(ack: Acknowledgment, record: ConsumerRecord<String, ByteArray>) = eventProcessor.process(
        ack = ack,
        consumerRecord = record,
        deserializationClazz = BalanceDepositedEvent::class.java,
        onError = eventProcessor.errorRetryHandler(kafkaTopics.accountBalanceDepositedRetry.name, DEFAULT_RETRY_COUNT)
    ) { event ->
        eventProcessor.on(
            ack = ack,
            consumerRecord = record,
            event = event,
            retryTopic = kafkaTopics.accountBalanceDepositedRetry.name
        )
    }

    @KafkaListener(
        groupId = "\${kafka.consumer-group-id:account_microservice_group_id}",
        topics = ["\${topics.accountBalanceDepositedRetry.name}"],
    )
    fun processRetry(ack: Acknowledgment, record: ConsumerRecord<String, ByteArray>) = eventProcessor.process(
        ack = ack,
        consumerRecord = record,
        deserializationClazz = BalanceDepositedEvent::class.java,
        onError = eventProcessor.errorRetryHandler(kafkaTopics.accountBalanceDepositedRetry.name, DEFAULT_RETRY_COUNT)
    ) { event ->
        eventProcessor.on(
            ack = ack,
            consumerRecord = record,
            event = event,
            retryTopic = kafkaTopics.accountBalanceDepositedRetry.name
        )
    }


    private companion object {
        private val log = KotlinLogging.logger { }
        private const val DEFAULT_RETRY_COUNT = 3
    }
}