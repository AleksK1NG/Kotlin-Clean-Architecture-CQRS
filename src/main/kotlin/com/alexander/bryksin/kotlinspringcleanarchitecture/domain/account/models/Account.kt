package com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.models

import arrow.core.Either
import arrow.core.raise.either
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.errors.*
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.valueObjects.*
import java.time.Instant

class Account(
    val accountId: AccountId = AccountId(),
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
        private set
    var createdAt: Instant? = null
        private set

    constructor(
        accountId: AccountId = AccountId(),
        contactInfo: ContactInfo = ContactInfo(),
        personalInfo: PersonalInfo = PersonalInfo(),
        address: Address = Address(),
        balance: Balance = Balance(),
        status: AccountStatus = AccountStatus.FREE,
        version: Long = 0,
        updatedAt: Instant? = null,
        createdAt: Instant? = null
    ) : this(accountId) {
        this.personalInfo = personalInfo
        this.contactInfo = contactInfo
        this.address = address
        this.balance = balance
        this.status = status
        this.version = version
        this.updatedAt = updatedAt
        this.createdAt = createdAt
    }

    fun depositBalance(newBalance: Balance): Either<AppError, Account> = either {
        if (balance.balanceCurrency != newBalance.balanceCurrency) raise(InvalidBalanceCurrency("invalid currency: $newBalance"))
        if (newBalance.amount < 0) raise(InvalidBalanceAmount("invalid balance amount: $newBalance"))

        balance = balance.copy(amount = (balance.amount + newBalance.amount))
        updatedAt = Instant.now()

        this@Account
    }

    fun withdrawBalance(newBalance: Balance): Either<AppError, Account> = either {
        if (balance.balanceCurrency != newBalance.balanceCurrency) raise(InvalidBalanceCurrency("invalid currency: $newBalance"))
        if (newBalance.amount < 0) raise(InvalidBalanceAmount("invalid balance amount: $newBalance"))

        val newAmount = (balance.amount - newBalance.amount)
        if ((newAmount) < 0) raise(InvalidBalanceError("invalid balance: $newBalance"))

        balance = balance.copy(amount = newAmount)
        updatedAt = Instant.now()

        this@Account
    }

    fun updateStatus(newStatus: AccountStatus): Either<AppError, Account> = either {
        status = newStatus
        updatedAt = Instant.now()
        this@Account
    }

    fun changeContactInfo(newContactInfo: ContactInfo): Either<AppError, Account> = either {
        contactInfo = newContactInfo
        updatedAt = Instant.now()
        this@Account
    }

    fun changeAddress(newAddress: Address): Either<AppError, Account> = either {
        address = newAddress
        updatedAt = Instant.now()
        this@Account
    }

    fun changePersonalInfo(newPersonalInfo: PersonalInfo): Either<AppError, Account> = either {
        personalInfo = newPersonalInfo
        updatedAt = Instant.now()
        this@Account
    }

    fun incVersion(amount: Long = 1): Either<AppError, Account> = either {
        if (amount < 1) raise(InvalidVersion("invalid version: $amount"))
        version += amount
        updatedAt = Instant.now()
        this@Account
    }

    fun withVersion(amount: Long = 1): Account {
        version = amount
        updatedAt = Instant.now()
        return this
    }

    fun decVersion(amount: Long = 1): Either<AppError, Account> = either {
        if (amount < 1) raise(InvalidVersion("invalid version: $amount"))
        version -= amount
        updatedAt = Instant.now()
        this@Account
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