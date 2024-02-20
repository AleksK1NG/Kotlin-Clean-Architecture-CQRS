package com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.events

import com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.events.BalanceDepositedEvent.Companion.ACCOUNT_BALANCE_DEPOSITED_V1
import com.alexander.bryksin.kotlinspringcleanarchitecture.application.common.serializer.Serializer
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.models.Account
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.valueObjects.AccountId
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.valueObjects.Balance
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.common.outbox.models.OutboxEvent
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


fun Account.toBalanceDepositedEvent(): BalanceDepositedEvent {
    return BalanceDepositedEvent(
        accountId = accountId!!,
        aggregateId = accountId.toString(),
        eventId = UUID.randomUUID().toString(),
        eventType = ACCOUNT_BALANCE_DEPOSITED_V1,
        timestamp = Instant.now(),
        version = version,

        balance = balance,
        updatedAt = updatedAt,
        createdAt = createdAt,
    )
}

fun BalanceDepositedEvent.toOutboxEvent(data: ByteArray): OutboxEvent = OutboxEvent(
    eventId = UUID.randomUUID(),
    eventType = ACCOUNT_BALANCE_DEPOSITED_V1,
    aggregateId = this.aggregateId,
    data = data,
    version = this.version,
    timestamp = Instant.now(),
)

fun BalanceDepositedEvent.toOutboxEvent(serializer: Serializer) = OutboxEvent(
    eventId = eventId.toUUID(),
    eventType = eventType,
    aggregateId = aggregateId,
    version = version,
    timestamp = Instant.now(),
    data = serializer.serializeToBytes(this)
)

fun String.toUUID(): UUID = UUID.fromString(this)