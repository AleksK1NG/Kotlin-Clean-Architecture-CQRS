package com.alexander.bryksin.kotlinspringcleanarchitecture.application.common.outbox.persistance

import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.common.outbox.models.OutboxEvent

interface OutboxRepository {
    suspend fun insert(event: OutboxEvent): OutboxEvent
    suspend fun deleteWithLock(event: OutboxEvent, callback: suspend (event: OutboxEvent) -> Unit): OutboxEvent
    suspend fun deleteEventsWithLock(
        batchSize: Int,
        callback: suspend (event: OutboxEvent) -> Unit
    )
}