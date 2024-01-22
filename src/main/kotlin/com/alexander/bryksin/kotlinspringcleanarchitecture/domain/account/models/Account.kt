package com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.models

import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.valueObjects.*
import java.time.OffsetDateTime

data class Account(
    val accountId: AccountId,
    val contactInfo: ContactInfo = ContactInfo(),
    val address: Address = Address(),
    val balance: Balance = Balance(),
    val status: AccountStatus = AccountStatus.FREE,

    val version: Long = 0,
    val updatedAt: OffsetDateTime? = null,
    val createdAt: OffsetDateTime? = null,
) {


    fun incBalance(amount: Long): Account {
        val newBalance = this.balance.copy(amount = (this.balance.amount + amount))
        return this.copy(balance = newBalance)
    }

    fun decBalance(amount: Long): Account {
        if ((this.balance.amount - amount) < 0) throw RuntimeException("invalid balance")
        val newBalance = this.balance.copy(amount = (this.balance.amount - amount))
        return this.copy(balance = newBalance)
    }


    fun changeContactInfo(contactInfo: ContactInfo): Account = this.copy(contactInfo = contactInfo)

    fun changeAddress(address: Address): Account = this.copy(address = address)

    companion object {}
}
