package com.alexander.bryksin.kotlinspringcleanarchitecture.infrastructure.outbox.persistance.entity

import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.outbox.models.OutboxEvent
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.OffsetDateTime


@Table(name = "outbox_table", schema = "microservices")
data class OutboxEntity(
    @field:Id @field:Column("event_id") val eventId: String,
    @field:Column("event_type") val eventType: String,
    @field:Column("aggregate_id") val aggregateId: String,
    @field:Column("version") val version: Long,
    @field:Column("timestamp") val timestamp: OffsetDateTime,
) {
    companion object {}
}

fun OutboxEntity.toEvent() = OutboxEvent(
    eventId = this.eventId,
    eventType = this.eventType,
    aggregateId = this.aggregateId,
    version = this.version,
    timestamp = this.timestamp
)

fun OutboxEvent.toEntity() = OutboxEntity(
    eventId = this.eventId,
    eventType = this.eventType,
    aggregateId = this.aggregateId,
    version = this.version,
    timestamp = this.timestamp
)