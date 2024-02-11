package com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.events

import com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.events.AccountStatusChangedEvent.Companion.ACCOUNT_STATUS_CHANGED_V1
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.models.Account
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.valueObjects.AccountId
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.valueObjects.AccountStatus
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.common.outbox.models.OutboxEvent
import java.time.Instant
import java.util.*

data class AccountStatusChangedEvent(
    val accountId: AccountId?,
    val status: AccountStatus = AccountStatus.FREE,
    val version: Long = 0,
    val updatedAt: Instant? = null,
    val createdAt: Instant? = null,
): AccountEvent {
    companion object {
        const val ACCOUNT_STATUS_CHANGED_V1 = "ACCOUNT_STATUS_CHANGED_V1"
    }
}

fun Account.toStatusChangedEvent() = AccountStatusChangedEvent(
    accountId = this.accountId,
    status = this.status,
    version = this.version,
    updatedAt = this.updatedAt,
    createdAt = this.createdAt,
)

fun AccountStatusChangedEvent.toOutboxEvent(data: ByteArray): OutboxEvent = OutboxEvent(
    eventId = UUID.randomUUID(),
    eventType = ACCOUNT_STATUS_CHANGED_V1,
    aggregateId = this.accountId?.id.toString(),
    data = data,
    version = this.version,
    timestamp = Instant.now(),
)