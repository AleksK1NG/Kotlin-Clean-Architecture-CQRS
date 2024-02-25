package com.alexander.bryksin.kotlinspringcleanarchitecture.api.account.kafka

import com.alexander.bryksin.kotlinspringcleanarchitecture.api.common.kafka.EventProcessor
import com.alexander.bryksin.kotlinspringcleanarchitecture.api.configuration.kafka.KafkaTopics
import com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.events.DomainStatusChangedEvent
import com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.services.AccountEventHandlerService
import io.github.oshai.kotlinlogging.KotlinLogging
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component


@Component
class AccountStatusChangedEventConsumer(
    private val accountEventHandlerService: AccountEventHandlerService,
    private val eventProcessor: EventProcessor,
    private val kafkaTopics: KafkaTopics
) {

    @KafkaListener(
        groupId = "\${kafka.consumer-group-id:account_microservice_group_id}",
        topics = ["\${topics.accountStatusChanged.name}"],
    )
    fun process(ack: Acknowledgment, record: ConsumerRecord<String, ByteArray>) = eventProcessor.process(
        ack = ack,
        consumerRecord = record,
        deserializationClazz = DomainStatusChangedEvent::class.java,
        onError = eventProcessor.errorRetryHandler(kafkaTopics.accountStatusChanged.name, DEFAULT_RETRY_COUNT)
    ) { event ->
        accountEventHandlerService.on(event)
        ack.acknowledge()
        log.info { "consumerRecord successfully processed: $record" }
    }


    @KafkaListener(
        groupId = "\${kafka.consumer-group-id:account_microservice_group_id}",
        topics = ["\${topics.accountStatusChangedRetry.name}"],
    )
    fun processRetry(ack: Acknowledgment, record: ConsumerRecord<String, ByteArray>) = eventProcessor.process(
        ack = ack,
        consumerRecord = record,
        deserializationClazz = DomainStatusChangedEvent::class.java,
        onError = eventProcessor.errorRetryHandler(kafkaTopics.accountStatusChangedRetry.name, DEFAULT_RETRY_COUNT)
    ) { event ->
        accountEventHandlerService.on(event)
        ack.acknowledge()
        log.info { "consumerRecord successfully processed: $record" }
    }


    private companion object {
        private val log = KotlinLogging.logger { }
        private const val DEFAULT_RETRY_COUNT = 3
    }
}