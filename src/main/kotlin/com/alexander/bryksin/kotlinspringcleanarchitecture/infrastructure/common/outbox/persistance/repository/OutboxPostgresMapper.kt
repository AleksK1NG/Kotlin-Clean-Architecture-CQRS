package com.alexander.bryksin.kotlinspringcleanarchitecture.infrastructure.common.outbox.persistance.repository

import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.common.outbox.models.OutboxEvent
import io.r2dbc.spi.Row
import java.math.BigInteger
import java.time.Instant
import java.util.*


fun Row.toOutboxEvent(): OutboxEvent = OutboxEvent(
    eventId = UUID.fromString(get("event_id", String::class.java)),
    eventType = get("event_type", String::class.java) ?: "",
    aggregateId = get("aggregate_id", String::class.java) ?: "",
    data = get("data", ByteArray::class.java) ?: byteArrayOf(),
    version = get("version", BigInteger::class.java)?.toLong() ?: 0,
    timestamp = get("timestamp", Instant::class.java) ?: Instant.now(),
)

fun OutboxEvent.toPostgresValuesMap() = mutableMapOf(
    "event_id" to eventId,
    "aggregate_id" to aggregateId,
    "event_type" to eventType,
    "version" to version,
    "data" to data,
    "timestamp" to timestamp,
)
