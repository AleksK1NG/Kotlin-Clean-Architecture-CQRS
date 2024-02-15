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
        val newBalance = balance.copy(amount = (this.balance.amount + amount))
        return copy(balance = newBalance, updatedAt = Instant.now())
    }

    fun withdrawBalance(amount: Long): Account {
        if ((balance.amount - amount) < 0) throw InvalidAmountException(accountId?.string() ?: "", amount)
        val newBalance = balance.copy(amount = (balance.amount - amount))
        return copy(balance = newBalance, updatedAt = Instant.now())
    }

    fun updateStatus(newStatus: AccountStatus) = copy(status = newStatus)

    fun changeContactInfo(contactInfo: ContactInfo): Account = copy(contactInfo = contactInfo, updatedAt = Instant.now())

    fun changeAddress(address: Address): Account = copy(address = address, updatedAt = Instant.now())

    fun changePersonalInfo(personalInfo: PersonalInfo): Account = copy(personalInfo = personalInfo, updatedAt = Instant.now())

    companion object {}
}
