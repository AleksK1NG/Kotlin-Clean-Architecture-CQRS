package com.alexander.bryksin.kotlinspringcleanarchitecture.domain.outbox.models

import java.time.OffsetDateTime

data class OutboxEvent(
    val eventId: String,
    val eventType: String,
    val aggregateId: String,
    val version: Long,
    val timestamp: OffsetDateTime,
) {
    companion object {}
}
