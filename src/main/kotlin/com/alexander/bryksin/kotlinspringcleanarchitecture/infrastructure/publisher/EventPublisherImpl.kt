package com.alexander.bryksin.kotlinspringcleanarchitecture.infrastructure.publisher

import arrow.core.Either
import com.alexander.bryksin.kotlinspringcleanarchitecture.application.common.publisher.EventPublisher
import com.alexander.bryksin.kotlinspringcleanarchitecture.application.common.serializer.Serializer
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.errors.AppError
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.common.outbox.models.OutboxEvent
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.common.scope.eitherScope
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

    override suspend fun publish(
        event: OutboxEvent,
        headers: Map<String, ByteArray>
    ): Either<AppError, Unit> = eitherScope<AppError, Unit> {
        val msg = ProducerRecord(event.kafkaTopic(), event.aggregateId, event.data)
        headers.forEach { (key, value) -> msg.headers().add(key, value) }
        kafkaTemplate.send(msg).await()
    }
        .onRight { log.info { "published outbox event: $it" } }
        .onLeft { log.error { "error while publishing event: $it" } }


    override suspend fun publish(events: List<OutboxEvent>): Either<AppError, Unit> = eitherScope {
        events.forEach { publish(it).bind() }
    }

    override suspend fun publish(
        topic: String,
        data: Any,
        headers: Map<String, ByteArray>,
    ): Either<AppError, Unit> = eitherScope<AppError, Unit> {
        val msg = ProducerRecord<String, ByteArray>(topic, serializer.serializeToBytes(data))
        headers.forEach { (key, value) -> msg.headers().add(key, value) }
        kafkaTemplate.send(msg).await()
    }
        .onRight { log.info { "published outbox event: topic: $topic" } }
        .onLeft { log.error { "error while publishing event: $it" } }


    override suspend fun publish(
        topic: String,
        key: String,
        data: Any,
        headers: Map<String, ByteArray>
    ): Either<AppError, Unit> = eitherScope<AppError, Unit> {
        val msg = ProducerRecord(topic, key, serializer.serializeToBytes(data))
        headers.forEach { (key, value) -> msg.headers().add(key, value) }
        kafkaTemplate.send(msg).await()
    }
        .onRight { log.info { "published outbox event: topic: $topic, key: $key" } }
        .onLeft { log.error { "error while publishing event: $it" } }

    override suspend fun publishBytes(
        topic: String,
        key: String,
        data: ByteArray,
        headers: Map<String, ByteArray>
    ): Either<AppError, Unit> = eitherScope<AppError, Unit> {
        val msg = ProducerRecord(topic, key, data)
        headers.forEach { (key, value) -> msg.headers().add(key, value) }
        kafkaTemplate.send(msg).await()
    }
        .onRight { log.info { "published outbox event: topic: $topic, key: $key" } }
        .onLeft { log.error { "error while publishing event: $it" } }


    private companion object {
        private val log = KotlinLogging.logger { }
    }
}