package com.alexander.bryksin.kotlinspringcleanarchitecture.application.outbox.persistance

import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.outbox.models.OutboxEvent

interface OutboxRepository {
    suspend fun insert(event: OutboxEvent): OutboxEvent
    suspend fun deleteWithLock(event: OutboxEvent, callback: suspend (event: OutboxEvent) -> Unit): OutboxEvent
}