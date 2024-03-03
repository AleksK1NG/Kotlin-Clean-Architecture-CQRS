package com.alexander.bryksin.kotlinspringcleanarchitecture.application.outbox.persistance

import arrow.core.Either
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.errors.AppError
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.outbox.models.OutboxEvent

interface OutboxRepository {

    suspend fun insert(event: OutboxEvent): Either<AppError, OutboxEvent>

    suspend fun deleteWithLock(
        event: OutboxEvent,
        callback: suspend (event: OutboxEvent) -> Either<AppError, Unit>
    ): Either<AppError, OutboxEvent>

    suspend fun deleteEventsWithLock(
        batchSize: Int,
        callback: suspend (event: OutboxEvent) -> Either<AppError, Unit>
    ): Either<AppError, Unit>
}