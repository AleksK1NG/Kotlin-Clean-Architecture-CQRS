package com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.models

import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.exceptions.InvalidAmountException
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.valueObjects.*
import java.time.Instant

data class Account(
    val accountId: AccountId?,
    val contactInfo: ContactInfo = ContactInfo(),
    val personalInfo: PersonalInfo = PersonalInfo(),
    val address: Address = Address(),
    val balance: Balance = Balance(),
    val status: AccountStatus = AccountStatus.FREE,

    val version: Long = 0,
    val updatedAt: Instant? = null,
    val createdAt: Instant? = null,
) {


    fun depositBalance(amount: Long): Account {
        val newBalance = this.balance.copy(amount = (this.balance.amount + amount))
        return this.copy(balance = newBalance)
    }

    fun withdrawBalance(amount: Long): Account {
        if ((this.balance.amount - amount) < 0) throw InvalidAmountException(accountId?.string() ?: "", amount)
        val newBalance = this.balance.copy(amount = (this.balance.amount - amount))
        return this.copy(balance = newBalance)
    }


    fun changeContactInfo(contactInfo: ContactInfo): Account = this.copy(contactInfo = contactInfo)

    fun changeAddress(address: Address): Account = this.copy(address = address)

    fun changePersonalInfo(personalInfo: PersonalInfo): Account = this.copy(personalInfo = personalInfo)

    companion object {}
}
