package com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.events

import java.time.Instant

sealed interface DomainEvent {
    val aggregateId: String
    val version: Long
    val eventId: String
    val eventType: String
    val timestamp: Instant
}