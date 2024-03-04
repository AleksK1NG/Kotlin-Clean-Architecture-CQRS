package com.alexander.bryksin.kotlinspringcleanarchitecture.api.account.http.contracts

import com.alexander.bryksin.kotlinspringcleanarchitecture.api.account.http.contracts.dto.ContactInfoRequest
import com.alexander.bryksin.kotlinspringcleanarchitecture.api.account.http.contracts.dto.toContactInfo
import com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.commands.ChangeContactInfoCommand
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.valueObjects.AccountId
import jakarta.validation.Valid

data class ChangeContactInfoRequest(@field:Valid val contactInfo: ContactInfoRequest) {
    companion object
}

fun ChangeContactInfoRequest.toCommand(accountId: AccountId) = ChangeContactInfoCommand(
    accountId = accountId,
    contactInfo = contactInfo.toContactInfo()
)