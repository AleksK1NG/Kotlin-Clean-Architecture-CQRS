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
    val version: Long = 0,
    val updatedAt: Instant? = null,
    val createdAt: Instant? = null,
) : AccountEvent {
    companion object {
        const val ACCOUNT_BALANCE_DEPOSITED_V1 = "ACCOUNT_BALANCE_DEPOSITED_V1"
    }
}


fun Account.toBalanceDepositedEvent(): BalanceDepositedEvent {
    return BalanceDepositedEvent(
        accountId = this.accountId!!,
        balance = this.balance,
        version = this.version,
        updatedAt = this.updatedAt,
        createdAt = this.createdAt,
    )
}

fun BalanceDepositedEvent.toOutboxEvent(data: ByteArray): OutboxEvent = OutboxEvent(
    eventId = UUID.randomUUID(),
    eventType = ACCOUNT_BALANCE_DEPOSITED_V1,
    aggregateId = this.accountId.id.toString(),
    data = data,
    version = this.version,
    timestamp = Instant.now(),
)

fun BalanceDepositedEvent.toOutboxEvent(serializer: Serializer) = OutboxEvent(
    eventId = UUID.randomUUID(),
    eventType = ACCOUNT_BALANCE_DEPOSITED_V1,
    aggregateId = accountId.id.toString(),
    version = version,
    timestamp = Instant.now(),
    data = serializer.serializeToBytes(this)
)