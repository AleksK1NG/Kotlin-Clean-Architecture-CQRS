package com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.commands

import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.models.Account
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.valueObjects.AccountId
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.valueObjects.Address
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.valueObjects.ContactInfo
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.valueObjects.PersonalInfo
import java.time.Instant
import java.util.*

data class CreateAccountCommand(
    val contactInfo: ContactInfo = ContactInfo(),
    val personalInfo: PersonalInfo = PersonalInfo(),
    val address: Address = Address(),
) : AccountDomainCommand {
    companion object {}
}

fun CreateAccountCommand.toAccount() = Account(
    accountId = AccountId(id = UUID.randomUUID()),
    contactInfo = contactInfo,
    address = address,
    personalInfo = personalInfo,
    updatedAt = Instant.now(),
    createdAt = Instant.now(),
    version = 1
)