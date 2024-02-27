package com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.events

import com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.events.BalanceWithdrawEvent.Companion.ACCOUNT_BALANCE_WITHDRAW_V1
import com.alexander.bryksin.kotlinspringcleanarchitecture.application.common.serializer.Serializer
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.models.Account
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.valueObjects.AccountId
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.valueObjects.Balance
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.common.outbox.models.OutboxEvent
import java.time.Instant
import java.util.*

data class BalanceWithdrawEvent(
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
        const val ACCOUNT_BALANCE_WITHDRAW_V1 = "ACCOUNT_BALANCE_WITHDRAW_V1"
    }
}


fun Account.toBalanceWithdrawEvent(newBalance: Balance): BalanceWithdrawEvent {
    return BalanceWithdrawEvent(
        accountId = accountId,
        aggregateId = accountId.id.toString(),
        eventId = UUID.randomUUID().toString(),
        eventType = ACCOUNT_BALANCE_WITHDRAW_V1,
        timestamp = Instant.now(),
        version = version,

        balance = newBalance,
        updatedAt = updatedAt,
        createdAt = createdAt,
    )
}


fun BalanceWithdrawEvent.toOutboxEvent(serializer: Serializer) = OutboxEvent(
    eventId = UUID.randomUUID(),
    eventType = ACCOUNT_BALANCE_WITHDRAW_V1,
    aggregateId = aggregateId,
    version = version,
    timestamp = Instant.now(),
    data = serializer.serializeToBytes(this)
)