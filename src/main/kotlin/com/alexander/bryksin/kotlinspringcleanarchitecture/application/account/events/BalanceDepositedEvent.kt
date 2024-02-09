package com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.events

import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.valueObjects.Balance
import java.time.OffsetDateTime

data class BalanceDepositedEvent(
    val accountId: String,
    val balance: Balance,
    val version: Long = 0,
    val updatedAt: OffsetDateTime? = null,
    val createdAt: OffsetDateTime? = null,
) {
    companion object {}
}
