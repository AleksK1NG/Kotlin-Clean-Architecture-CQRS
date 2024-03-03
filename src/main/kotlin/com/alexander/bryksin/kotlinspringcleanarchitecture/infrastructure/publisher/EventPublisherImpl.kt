package com.alexander.bryksin.kotlinspringcleanarchitecture.infrastructure.publisher

import arrow.core.Either
import com.alexander.bryksin.kotlinspringcleanarchitecture.api.configuration.kafka.KafkaTopics
import com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.events.*
import com.alexander.bryksin.kotlinspringcleanarchitecture.application.common.publisher.EventPublisher
import com.alexander.bryksin.kotlinspringcleanarchitecture.application.common.serializer.Serializer
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.errors.AppError
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.outbox.models.OutboxEvent
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.common.scope.eitherScope
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.future.await
import org.apache.kafka.clients.producer.ProducerRecord
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component


@Component
class EventPublisherImpl(
    private val kafkaTemplate: KafkaTemplate<String, ByteArray>,
    private val serializer: Serializer,
    private val kafkaTopics: KafkaTopics
) : EventPublisher {

    override suspend fun publish(
        event: OutboxEvent,
        headers: Map<String, ByteArray>
    ): Either<AppError, Unit> = eitherScope<AppError, Unit> {
        val msg = ProducerRecord(event.kafkaTopic(kafkaTopics), event.aggregateId, event.data)
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

internal fun OutboxEvent.kafkaTopic(kafkaTopics: KafkaTopics) = when (eventType) {
    AccountCreatedEvent.ACCOUNT_CREATED_EVENT_V1 -> kafkaTopics.accountCreated.name
    AccountStatusChangedEvent.ACCOUNT_STATUS_CHANGED_V1 -> kafkaTopics.accountStatusChanged.name
    BalanceDepositedEvent.ACCOUNT_BALANCE_DEPOSITED_V1 -> kafkaTopics.accountBalanceDeposited.name
    BalanceWithdrawEvent.ACCOUNT_BALANCE_WITHDRAW_V1 -> kafkaTopics.accountBalanceWithdraw.name
    ContactInfoChangedEvent.ACCOUNT_CONTACT_INFO_CHANGED_V1 -> kafkaTopics.accountContactInfoChanged.name
    PersonalInfoUpdatedEvent.ACCOUNT_PERSONAL_INFO_UPDATED_V1 -> kafkaTopics.accountInfoUpdated.name
    else -> kafkaTopics.deadLetterQueue.name
}

