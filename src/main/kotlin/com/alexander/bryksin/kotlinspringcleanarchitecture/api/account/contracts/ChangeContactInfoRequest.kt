package com.alexander.bryksin.kotlinspringcleanarchitecture.api.account.contracts

import com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.commands.ChangeContactInfoCommand
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.valueObjects.AccountId
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.valueObjects.ContactInfo

data class ChangeContactInfoRequest(val contactInfo: ContactInfo) {
    companion object {}
}

fun ChangeContactInfoRequest.toCommand(accountId: AccountId) = ChangeContactInfoCommand(
    accountId = accountId,
    contactInfo = contactInfo
)