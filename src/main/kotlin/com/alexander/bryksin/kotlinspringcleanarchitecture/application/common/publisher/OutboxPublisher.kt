package com.alexander.bryksin.kotlinspringcleanarchitecture.application.common.publisher

import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.common.outbox.models.OutboxEvent

interface OutboxPublisher {
    suspend fun publish(event: OutboxEvent)
    suspend fun publish(events: List<OutboxEvent>)

    suspend fun publish(topic: String, data: Any, headers: Map<String, ByteArray> = mapOf())
    suspend fun publish(topic: String, key: String, data: Any, headers: Map<String, ByteArray> = mapOf())
}