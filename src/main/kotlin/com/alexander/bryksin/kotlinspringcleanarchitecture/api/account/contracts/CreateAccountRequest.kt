package com.alexander.bryksin.kotlinspringcleanarchitecture.api.account.contracts

import com.alexander.bryksin.kotlinspringcleanarchitecture.api.account.contracts.dto.*
import com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.commands.CreateAccountCommand

data class CreateAccountRequest(
    val contactInfo: ContactInfoRequest,
    val personalInfo: PersonalInfoRequest,
    val address: AddressRequest,
) {
    companion object
}

fun CreateAccountRequest.toCommand() = CreateAccountCommand(
   contactInfo = contactInfo.toContactInfo(),
    personalInfo = personalInfo.toPersonalInfo(),
    address = address.toAddress(),
)