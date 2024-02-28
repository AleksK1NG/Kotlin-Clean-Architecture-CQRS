package com.alexander.bryksin.kotlinspringcleanarchitecture.domain.common.outbox.models

import java.time.Instant
import java.util.*

data class OutboxEvent(
    val eventId: UUID,
    val eventType: String,
    val aggregateId: String,
    val data: ByteArray,
    val version: Long,
    val timestamp: Instant,
) {
    companion object  {}

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as OutboxEvent

        if (eventId != other.eventId) return false
        if (eventType != other.eventType) return false
        if (aggregateId != other.aggregateId) return false
        if (version != other.version) return false
        if (timestamp != other.timestamp) return false

        return true
    }

    override fun hashCode(): Int {
        var result = eventId.hashCode()
        result = 31 * result + eventType.hashCode()
        result = 31 * result + aggregateId.hashCode()
        result = 31 * result + version.hashCode()
        result = 31 * result + timestamp.hashCode()
        return result
    }
}
