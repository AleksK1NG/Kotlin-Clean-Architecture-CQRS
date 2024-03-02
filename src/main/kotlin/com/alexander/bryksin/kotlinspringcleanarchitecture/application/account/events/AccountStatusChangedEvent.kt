package com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.events

import com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.events.AccountStatusChangedEvent.Companion.ACCOUNT_STATUS_CHANGED_V1
import com.alexander.bryksin.kotlinspringcleanarchitecture.application.common.serializer.Serializer
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.models.Account
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.valueObjects.AccountId
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.valueObjects.AccountStatus
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.common.outbox.models.OutboxEvent
import java.time.Instant
import java.util.*

data class AccountStatusChangedEvent(
    val accountId: AccountId,
    val status: AccountStatus = AccountStatus.FREE,
    val updatedAt: Instant? = null,
    val createdAt: Instant? = null,

    override val eventId: String,
    override val version: Long = 0,
    override val aggregateId: String,
    override val eventType: String,
    override val timestamp: Instant,
) : DomainEvent {
    companion object {
        const val ACCOUNT_STATUS_CHANGED_V1 = "ACCOUNT_STATUS_CHANGED_V1"
    }
}

fun Account.toStatusChangedEvent(): AccountStatusChangedEvent {
    return AccountStatusChangedEvent(
        accountId = accountId,
        aggregateId = accountId.id.toString(),
        eventId = UUID.randomUUID().toString(),
        eventType = ACCOUNT_STATUS_CHANGED_V1,
        timestamp = Instant.now(),
        version = version,
        status = status,
        updatedAt = updatedAt,
        createdAt = createdAt,
    )
}

fun Account.toStatusChangedOutboxEvent(serializer: Serializer): OutboxEvent {
    return this.toStatusChangedEvent().toOutboxEvent(serializer)
}

fun AccountStatusChangedEvent.toOutboxEvent(serializer: Serializer) = OutboxEvent(
    eventId = UUID.randomUUID(),
    eventType = ACCOUNT_STATUS_CHANGED_V1,
    aggregateId = aggregateId,
    version = version,
    timestamp = Instant.now(),
    data = serializer.serializeToBytes(this)
)