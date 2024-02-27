package com.alexander.bryksin.kotlinspringcleanarchitecture.api.account.contracts

import com.alexander.bryksin.kotlinspringcleanarchitecture.api.account.contracts.dto.ContactInfoRequest
import com.alexander.bryksin.kotlinspringcleanarchitecture.api.account.contracts.dto.toContactInfo
import com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.commands.ChangeContactInfoCommand
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.valueObjects.AccountId

data class ChangeContactInfoRequest(val contactInfo: ContactInfoRequest) {
    companion object
}

fun ChangeContactInfoRequest.toCommand(accountId: AccountId) = ChangeContactInfoCommand(
    accountId = accountId,
    contactInfo = contactInfo.toContactInfo()
)