package com.alexander.bryksin.kotlinspringcleanarchitecture.api.account.contracts

import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.models.Account
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.valueObjects.*
import java.time.OffsetDateTime

data class AccountHttpResponse(
    val accountId: AccountId,
    val contactInfo: ContactInfo = ContactInfo(),
    val personalInfo: PersonalInfo = PersonalInfo(),
    val address: Address = Address(),
    val balance: Balance = Balance(),
    val status: AccountStatus = AccountStatus.FREE,

    val version: Long = 0,
    val updatedAt: OffsetDateTime? = null,
    val createdAt: OffsetDateTime? = null,
) {
    companion object
}

fun Account.toHttpResponse() = AccountHttpResponse(
    accountId = this.accountId,
    contactInfo = this.contactInfo,
    personalInfo = this.personalInfo,
    address = this.address,
    balance = this.balance,
    status = this.status,
    version = this.version,
    updatedAt = this.updatedAt,
    createdAt = this.createdAt,
)
