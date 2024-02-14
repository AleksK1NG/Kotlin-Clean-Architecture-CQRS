package com.alexander.bryksin.kotlinspringcleanarchitecture.api.account.kafka

import com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.events.BalanceDepositedEvent
import com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.services.AccountEventsHandler
import com.alexander.bryksin.kotlinspringcleanarchitecture.application.common.publisher.EventPublisher
import com.alexander.bryksin.kotlinspringcleanarchitecture.application.common.serializer.SerializationException
import com.alexander.bryksin.kotlinspringcleanarchitecture.application.common.serializer.Serializer
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.common.outbox.models.OutboxEvent
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.runBlocking
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component

@Component
class BalanceDepositedEventConsumer(
    private val serializer: Serializer,
    private val accountEventsHandler: AccountEventsHandler,
    private val eventPublisher: EventPublisher,
) {

   private inline fun <reified T> deserializeRecordToEvent(consumerRecord: ConsumerRecord<String, ByteArray>): T {
       val outboxEvent = serializer.deserialize(consumerRecord.value(), OutboxEvent::class.java)
       return serializer.deserialize(outboxEvent.data, T::class.java)
   }

    @KafkaListener(
        groupId = "\${kafka.consumer-group-id:account_microservice_group_id}",
        topics = ["\${topics.accountBalanceDeposited.name}"],
    )
    fun process(ack: Acknowledgment, consumerRecord: ConsumerRecord<String, ByteArray>) = runBlocking {
        try {
            log.info { "processing ${consumerRecord.topic()} value: ${String(consumerRecord.value())}" }
            val balanceDepositedEvent = deserializeRecordToEvent<BalanceDepositedEvent>(consumerRecord)
            accountEventsHandler.on(balanceDepositedEvent)
            ack.acknowledge()
        } catch (e: Exception) {
            log.error { "error while processing event: ${e.message}" }
            if (unprocessableExceptions.contains(e::class.java)) {
                log.warn { "publishing to DLQ: ${e.message}" }
//                eventPublisher.publish()
                ack.acknowledge()
                return@runBlocking
            }
        }
    }


    private companion object {
        private val log = KotlinLogging.logger { }
        private val unprocessableExceptions = setOf(SerializationException::class.java)
    }
}