package com.alexander.bryksin.kotlinspringcleanarchitecture.infrastructure.publisher

import com.alexander.bryksin.kotlinspringcleanarchitecture.application.common.publisher.OutboxPublisher
import com.alexander.bryksin.kotlinspringcleanarchitecture.application.common.serializer.Serializer
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.common.outbox.models.OutboxEvent
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.future.await
import org.apache.kafka.clients.producer.ProducerRecord
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component


@Component
class OutboxPublisherImpl(
    private val kafkaTemplate: KafkaTemplate<String, ByteArray>,
    private val serializer: Serializer
) : OutboxPublisher {

    override suspend fun publish(event: OutboxEvent) {
        log.info { "Publishing outbox event: $event" }
    }

    override suspend fun publish(events: List<OutboxEvent>) {
        log.info { "Publishing outbox events: $events" }
    }

    override suspend fun publish(topic: String, data: Any, headers: Map<String, ByteArray>) {
        val msg = ProducerRecord<String, ByteArray>(topic, serializer.serializeToBytes(data))
        headers.forEach { (key, value) -> msg.headers().add(key, value) }
        kafkaTemplate.send(msg).await().also { log.info { "Published outbox event: $it" } }
    }

    override suspend fun publish(topic: String, key: String, data: Any, headers: Map<String, ByteArray>) {
        try {
            val msg = ProducerRecord(topic, key, serializer.serializeToBytes(data))
            headers.forEach { (key, value) -> msg.headers().add(key, value) }
            kafkaTemplate.send(msg).await().also { log.info { "Published outbox event: $it" } }
        } catch (e: Exception) {
            log.error { "error while publishing event: ${e.message}" }
            throw e
        }
    }


    private companion object {
        private val log = KotlinLogging.logger { }
    }
}