package com.alexander.bryksin.kotlinspringcleanarchitecture.application.common.publisher

import arrow.core.Either
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.errors.AppError
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.outbox.models.OutboxEvent

interface EventPublisher {

    suspend fun publish(event: OutboxEvent, headers: Map<String, ByteArray> = mapOf()): Either<AppError, Unit>

    suspend fun publish(events: List<OutboxEvent>): Either<AppError, Unit>

    suspend fun publish(topic: String, data: Any, headers: Map<String, ByteArray> = mapOf()): Either<AppError, Unit>

    suspend fun publish(
        topic: String,
        key: String,
        data: Any,
        headers: Map<String, ByteArray> = mapOf()
    ): Either<AppError, Unit>

    suspend fun publishBytes(
        topic: String,
        key: String,
        data: ByteArray,
        headers: Map<String, ByteArray> = mapOf()
    ): Either<AppError, Unit>
}