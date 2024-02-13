package com.alexander.bryksin.kotlinspringcleanarchitecture.api.account.kafka

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.runBlocking
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component


@Component
class AccountConsumer {

    @KafkaListener(
        groupId = "\${kafka.consumer-group-id:account_microservice_group_id}",
        topics = [
            "\${topics.accountCreated.name}",
            "\${topics.accountCreatedRetry.name}",
        ],
        id = "orders-consumer"
    )
    fun process(ack: Acknowledgment, consumerRecord: ConsumerRecord<String, ByteArray>) = runBlocking {
        log.info { "processing ${consumerRecord.topic()} value: ${String(consumerRecord.value())}" }
        ack.acknowledge()
    }


    private companion object {
        private val log = KotlinLogging.logger { }
    }
}
