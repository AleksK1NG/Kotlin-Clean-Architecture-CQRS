package com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.commands

import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.valueObjects.AccountId
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.valueObjects.ContactInfo

data class ChangeContactInfoCommand(val accountId: AccountId, val contactInfo: ContactInfo) : AccountDomainCommand {
    companion object {}
}
