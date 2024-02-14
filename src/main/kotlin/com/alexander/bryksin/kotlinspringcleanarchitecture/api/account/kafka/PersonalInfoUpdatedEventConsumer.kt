package com.alexander.bryksin.kotlinspringcleanarchitecture.api.account.kafka

import com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.events.PersonalInfoUpdatedEvent
import com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.services.AccountEventsHandler
import io.github.oshai.kotlinlogging.KotlinLogging
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component


@Component
class PersonalInfoUpdatedEventConsumer(
    private val eventProcessor: EventProcessor,
    private val accountEventsHandler: AccountEventsHandler,
)  {

    @KafkaListener(
        groupId = "\${kafka.consumer-group-id:account_microservice_group_id}",
        topics = ["\${topics.accountInfoUpdated.name}"],
    )
    fun process(ack: Acknowledgment, record: ConsumerRecord<String, ByteArray>) = eventProcessor.runProcess(
        ack = ack,
        consumerRecord = record,
        deserializationClazz = PersonalInfoUpdatedEvent::class.java,
        onError = {}
    ) { event ->
        accountEventsHandler.on(event)
        ack.acknowledge()
        log.info { "consumerRecord successfully processed: $record" }
    }


    private companion object {
        private val log = KotlinLogging.logger { }
    }
}