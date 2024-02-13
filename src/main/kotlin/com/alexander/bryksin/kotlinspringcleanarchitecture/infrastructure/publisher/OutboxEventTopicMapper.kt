package com.alexander.bryksin.kotlinspringcleanarchitecture.infrastructure.publisher

import com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.events.AccountCreatedEvent.Companion.ACCOUNT_CREATED_EVENT_V1
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.common.outbox.models.OutboxEvent

fun OutboxEvent.kafkaTopic() = when (eventType) {
    (ACCOUNT_CREATED_EVENT_V1) -> "account-created"
    else -> "unknown"
}