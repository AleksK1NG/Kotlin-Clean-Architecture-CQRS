package com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.models

import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.exceptions.InvalidAmountException
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.exceptions.InvalidCurrencyException
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.exceptions.InvalidVersionException
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.valueObjects.*
import java.time.Instant

class Account(
    var accountId: AccountId? = null,
) {
    var contactInfo: ContactInfo = ContactInfo()
        private set
    var personalInfo: PersonalInfo = PersonalInfo()
        private set
    var address: Address = Address()
        private set
    var balance: Balance = Balance()
        private set
    var status: AccountStatus = AccountStatus.FREE
        private set

    var version: Long = 0
        private set

    var updatedAt: Instant? = null
    var createdAt: Instant? = null


    constructor(
        accountId: AccountId? = null,
        contactInfo: ContactInfo = ContactInfo(),
        personalInfo: PersonalInfo = PersonalInfo(),
        address: Address = Address(),
        balance: Balance = Balance(),
        status: AccountStatus = AccountStatus.FREE,
        version: Long = 0,
        updatedAt: Instant? = null,
        createdAt: Instant? = null
    ) : this() {
        this.accountId = accountId
        this.personalInfo = personalInfo
        this.contactInfo = contactInfo
        this.address = address
        this.balance = balance
        this.status = status
        this.version = version
        this.updatedAt = updatedAt
        this.createdAt = createdAt
    }

    fun depositBalance(newBalance: Balance): Account {
        if (balance.balanceCurrency != newBalance.balanceCurrency) throw InvalidCurrencyException(
            balance.balanceCurrency,
            newBalance.balanceCurrency
        )

        if (newBalance.amount < 0) throw InvalidAmountException(accountId?.string() ?: "", newBalance.amount)
        balance = balance.copy(amount = (balance.amount + newBalance.amount))
        updatedAt = Instant.now()
        return this
    }

    fun withdrawBalance(newBalance: Balance): Account {
        if (balance.balanceCurrency != newBalance.balanceCurrency) throw InvalidCurrencyException(
            balance.balanceCurrency,
            newBalance.balanceCurrency
        )

        val newAmount = (balance.amount - newBalance.amount)
        if ((newAmount) < 0) throw InvalidAmountException(accountId?.string() ?: "", newBalance.amount)
        balance = balance.copy(amount = newAmount)
        updatedAt = Instant.now()
        return this
    }

    fun updateStatus(newStatus: AccountStatus): Account {
        status = newStatus
        updatedAt = Instant.now()
        return this
    }

    fun changeContactInfo(newContactInfo: ContactInfo): Account {
        contactInfo = newContactInfo
        updatedAt = Instant.now()
        return this
    }

    fun changeAddress(newAddress: Address): Account {
        address = newAddress
        updatedAt = Instant.now()
        return this
    }

    fun changePersonalInfo(newPersonalInfo: PersonalInfo): Account {
        personalInfo = newPersonalInfo
        updatedAt = Instant.now()
        return this
    }

    fun incVersion(amount: Long = 1): Account {
        if (amount < 1) throw InvalidVersionException(accountId, amount)
        version += amount
        updatedAt = Instant.now()
        return this
    }

    fun withVersion(amount: Long = 1): Account {
        if (amount < 0) throw InvalidVersionException(accountId, amount)
        version = amount
        updatedAt = Instant.now()
        return this
    }

    fun decVersion(amount: Long = 1): Account {
        if (amount < 1) throw InvalidVersionException(accountId, amount)
        version -= amount
        updatedAt = Instant.now()
        return this
    }

    fun withUpdatedAt(newValue: Instant): Account {
        updatedAt = newValue
        return this
    }

    override fun toString(): String {
        return "Account(accountId=$accountId, contactInfo=$contactInfo, personalInfo=$personalInfo, address=$address, balance=$balance, status=$status, version=$version, updatedAt=$updatedAt, createdAt=$createdAt)"
    }

    companion object {}



}
