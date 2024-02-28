package com.alexander.bryksin.kotlinspringcleanarchitecture.api.account.contracts

import com.alexander.bryksin.kotlinspringcleanarchitecture.api.account.contracts.dto.*
import com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.commands.CreateAccountCommand
import jakarta.validation.Valid

data class CreateAccountRequest(
    @field:Valid val contactInfo: ContactInfoRequest,
    @field:Valid val personalInfo: PersonalInfoRequest,
    @field:Valid val address: AddressRequest,
) {
    companion object
}

fun CreateAccountRequest.toCommand() = CreateAccountCommand(
    contactInfo = contactInfo.toContactInfo(),
    personalInfo = personalInfo.toPersonalInfo(),
    address = address.toAddress(),
)