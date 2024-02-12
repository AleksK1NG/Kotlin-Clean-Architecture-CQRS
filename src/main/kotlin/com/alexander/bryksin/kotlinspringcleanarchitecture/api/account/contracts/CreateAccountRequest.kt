package com.alexander.bryksin.kotlinspringcleanarchitecture.api.account.contracts

import com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.commands.CreateAccountCommand
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.valueObjects.Address
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.valueObjects.ContactInfo
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.valueObjects.PersonalInfo

data class CreateAccountRequest(
    val contactInfo: ContactInfo,
    val personalInfo: PersonalInfo,
    val address: Address,
) {
    companion object {}
}

fun CreateAccountRequest.toCommand() = CreateAccountCommand(
   contactInfo = contactInfo,
    personalInfo = personalInfo,
    address = address,
)