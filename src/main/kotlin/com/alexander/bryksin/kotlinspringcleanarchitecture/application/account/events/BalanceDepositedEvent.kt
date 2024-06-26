package com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.events

import com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.events.BalanceDepositedEvent.Companion.ACCOUNT_BALANCE_DEPOSITED_V1
import com.alexander.bryksin.kotlinspringcleanarchitecture.application.common.serializer.Serializer
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.models.Account
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.valueObjects.AccountId
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.valueObjects.Balance
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.outbox.models.OutboxEvent
import java.time.Instant
import java.util.*

data class BalanceDepositedEvent(
    val accountId: AccountId,
    val balance: Balance,
    val updatedAt: Instant? = null,
    val createdAt: Instant? = null,

    override val eventId: String,
    override val eventType: String,
    override val aggregateId: String,
    override val version: Long = 0,
    override val timestamp: Instant,
) : DomainEvent {
    companion object {
        const val ACCOUNT_BALANCE_DEPOSITED_V1 = "ACCOUNT_BALANCE_DEPOSITED_V1"
    }
}


fun Account.toBalanceDepositedEvent(newBalance: Balance): BalanceDepositedEvent {
    return BalanceDepositedEvent(
        accountId = accountId,
        aggregateId = accountId.id.toString(),
        eventId = UUID.randomUUID().toString(),
        eventType = ACCOUNT_BALANCE_DEPOSITED_V1,
        timestamp = Instant.now(),
        version = version,

        balance = newBalance,
        updatedAt = updatedAt,
        createdAt = createdAt,
    )
}

fun Account.toBalanceDepositedOutboxEvent(newBalance: Balance, serializer: Serializer): OutboxEvent {
    return this.toBalanceDepositedEvent(newBalance).toOutboxEvent(serializer)
}

fun BalanceDepositedEvent.toOutboxEvent(serializer: Serializer) = OutboxEvent(
    eventId = eventId.toUUID(),
    eventType = eventType,
    aggregateId = aggregateId,
    version = version,
    timestamp = Instant.now(),
    data = serializer.serializeToBytes(this)
)

fun String.toUUID(): UUID = UUID.fromString(this)