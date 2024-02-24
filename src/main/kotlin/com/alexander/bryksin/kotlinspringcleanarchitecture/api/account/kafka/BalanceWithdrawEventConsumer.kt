package com.alexander.bryksin.kotlinspringcleanarchitecture.api.account.kafka

import com.alexander.bryksin.kotlinspringcleanarchitecture.api.common.kafka.EventProcessor
import com.alexander.bryksin.kotlinspringcleanarchitecture.api.configuration.kafka.KafkaTopics
import com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.events.BalanceWithdrawEvent
import com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.services.AccountEventHandlerService
import io.github.oshai.kotlinlogging.KotlinLogging
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component


@Component
class BalanceWithdrawEventConsumer(
    private val eventProcessor: EventProcessor,
    private val accountEventHandlerService: AccountEventHandlerService,
    private val kafkaTopics: KafkaTopics
) {

    @KafkaListener(
        groupId = "\${kafka.consumer-group-id:account_microservice_group_id}",
        topics = ["\${topics.accountBalanceWithdraw.name}"],
    )
    fun process(ack: Acknowledgment, record: ConsumerRecord<String, ByteArray>) = eventProcessor.process(
        ack = ack,
        consumerRecord = record,
        deserializationClazz = BalanceWithdrawEvent::class.java,
        onError = eventProcessor.defaultErrorRetryHandler(kafkaTopics.accountBalanceWithdraw.name, DEFAULT_RETRY_COUNT)
    ) { event ->
        accountEventHandlerService.on(event)
        ack.acknowledge()
        log.info { "consumerRecord successfully processed: $record" }
    }

    @KafkaListener(
        groupId = "\${kafka.consumer-group-id:account_microservice_group_id}",
        topics = ["\${topics.accountBalanceWithdrawRetry.name}"],
    )
    fun processRetry(ack: Acknowledgment, record: ConsumerRecord<String, ByteArray>) = eventProcessor.process(
        ack = ack,
        consumerRecord = record,
        deserializationClazz = BalanceWithdrawEvent::class.java,
        onError = eventProcessor.defaultErrorRetryHandler(kafkaTopics.accountBalanceWithdrawRetry.name, DEFAULT_RETRY_COUNT)
    ) { event ->
        accountEventHandlerService.on(event)
        ack.acknowledge()
        log.info { "consumerRecord successfully processed: $record" }
    }


    private companion object {
        private const val DEFAULT_RETRY_COUNT = 3
        private val log = KotlinLogging.logger { }
    }
}