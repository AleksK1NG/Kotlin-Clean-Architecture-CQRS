package com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.events

import com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.events.BalanceWithdrawEvent.Companion.ACCOUNT_BALANCE_WITHDRAW_V1
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.models.Account
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.valueObjects.AccountId
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.valueObjects.Balance
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.common.outbox.models.OutboxEvent
import java.time.Instant
import java.util.*

data class BalanceWithdrawEvent(
    val accountId: AccountId?,
    val balance: Balance,
    val version: Long = 0,
    val updatedAt: Instant? = null,
    val createdAt: Instant? = null,
): AccountEvent {
    companion object {
        const val ACCOUNT_BALANCE_WITHDRAW_V1 = "ACCOUNT_BALANCE_WITHDRAW_V1"
    }
}


fun Account.toBalanceWithdrawEvent() = BalanceWithdrawEvent(
    accountId = this.accountId,
    balance = this.balance,
    version = this.version,
    updatedAt = this.updatedAt,
    createdAt = this.createdAt,
)

fun BalanceWithdrawEvent.toOutboxEvent(data: ByteArray): OutboxEvent = OutboxEvent(
    eventId = UUID.randomUUID(),
    eventType = ACCOUNT_BALANCE_WITHDRAW_V1,
    aggregateId = this.accountId?.id.toString(),
    data = data,
    version = this.version,
    timestamp = Instant.now(),
)