package com.alexander.bryksin.kotlinspringcleanarchitecture.infrastructure.outbox.persistance.entity

import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.outbox.models.OutboxEvent
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant
import java.util.*


@Table(name = "outbox_table", schema = "microservices")
data class OutboxEntity(
    @field:Id @field:Column("event_id") val eventId: UUID? = null,
    @field:Column("event_type") val eventType: String,
    @field:Column("aggregate_id") val aggregateId: String,
    @field:Column("version") val version: Long,
    @field:Column("data") val data: ByteArray,
    @field:Column("timestamp") val timestamp: Instant,
) {
    companion object {}

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as OutboxEntity

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

fun OutboxEntity.toEvent() = OutboxEvent(
    eventId = this.eventId,
    eventType = this.eventType,
    aggregateId = this.aggregateId,
    version = this.version,
    timestamp = this.timestamp,
    data = this.data
)

fun OutboxEvent.toEntity() = OutboxEntity(
    eventId = this.eventId,
    eventType = this.eventType,
    aggregateId = this.aggregateId,
    version = this.version,
    timestamp = this.timestamp,
    data = this.data
)