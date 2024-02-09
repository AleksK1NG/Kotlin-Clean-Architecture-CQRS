package com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.events

import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.valueObjects.AccountId
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.valueObjects.AccountStatus
import java.time.OffsetDateTime

data class AccountStatusChangedEvent(
    val accountId: AccountId,
    val status: AccountStatus = AccountStatus.FREE,
    val version: Long = 0,
    val updatedAt: OffsetDateTime? = null,
    val createdAt: OffsetDateTime? = null,
) {
    companion object
}
