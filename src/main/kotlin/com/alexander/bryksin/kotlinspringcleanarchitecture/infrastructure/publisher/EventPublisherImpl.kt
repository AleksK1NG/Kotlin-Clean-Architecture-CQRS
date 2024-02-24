package com.alexander.bryksin.kotlinspringcleanarchitecture.infrastructure.publisher

import com.alexander.bryksin.kotlinspringcleanarchitecture.application.common.publisher.EventPublisher
import com.alexander.bryksin.kotlinspringcleanarchitecture.application.common.serializer.Serializer
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.common.outbox.models.OutboxEvent
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.common.utils.runSuspendCatching
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.future.await
import org.apache.kafka.clients.producer.ProducerRecord
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component


@Component
class EventPublisherImpl(
    private val kafkaTemplate: KafkaTemplate<String, ByteArray>,
    private val serializer: Serializer
) : EventPublisher {

    override suspend fun publish(event: OutboxEvent, headers: Map<String, ByteArray>): Unit =
        runSuspendCatching {
            val msg = ProducerRecord(event.kafkaTopic(), event.aggregateId, event.data)
            headers.forEach { (key, value) -> msg.headers().add(key, value) }
            kafkaTemplate.send(msg).await()
        }
            .onSuccess { log.info { "published outbox event: $it" } }
            .onFailure { log.error { "error while publishing event: ${it.message}" } }
            .map { }
            .getOrThrow()

    override suspend fun publish(events: List<OutboxEvent>) {
        events.forEach { publish(it) }
    }

    override suspend fun publish(topic: String, data: Any, headers: Map<String, ByteArray>) = runSuspendCatching {
        val msg = ProducerRecord<String, ByteArray>(topic, serializer.serializeToBytes(data))
        headers.forEach { (key, value) -> msg.headers().add(key, value) }
        kafkaTemplate.send(msg).await().also { log.info { "Published outbox event: $it" } }
    }
        .onSuccess { log.info { "published outbox event: $it" } }
        .onFailure { log.error { "error while publishing event: ${it.message}" } }
        .map { }
        .getOrThrow()


    override suspend fun publish(topic: String, key: String, data: Any, headers: Map<String, ByteArray>) =
        runSuspendCatching {
            val msg = ProducerRecord(topic, key, serializer.serializeToBytes(data))
            headers.forEach { (key, value) -> msg.headers().add(key, value) }
            kafkaTemplate.send(msg).await().also { log.info { "Published outbox event: $it" } }
        }
            .onSuccess { log.info { "published outbox event: $it" } }
            .onFailure { log.error { "error while publishing event: ${it.message}" } }
            .map { }
            .getOrThrow()


    override suspend fun publishBytes(topic: String, key: String, data: ByteArray, headers: Map<String, ByteArray>) =
        runSuspendCatching {
            val msg = ProducerRecord(topic, key, data)
            headers.forEach { (key, value) -> msg.headers().add(key, value) }
            kafkaTemplate.send(msg).await().also { log.info { "Published outbox event: $it" } }
        }
            .onSuccess { log.info { "published outbox event: $it" } }
            .onFailure { log.error { "error while publishing event: ${it.message}" } }
            .map { }
            .getOrThrow()


    private companion object {
        private val log = KotlinLogging.logger { }
    }
}