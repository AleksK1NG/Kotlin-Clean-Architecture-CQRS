package com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.commands

import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.models.Account
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.valueObjects.AccountId
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.valueObjects.Address
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.valueObjects.ContactInfo
import java.time.Instant
import java.util.*

data class CreateAccountCommand(
    val email: String,
    val phone: String,
    val country: String,
    val city: String,
    val postCode: String,
) {
    companion object {}
}

fun CreateAccountCommand.toAccount() = Account(
    accountId = AccountId(id = UUID.randomUUID()),
    contactInfo = ContactInfo(
        email = this.email,
        phone = this.phone,
    ),
    address = Address(
        country = this.country,
        city = this.city,
        postCode = this.postCode,
    ),
    updatedAt = Instant.now(),
    createdAt = Instant.now()
)