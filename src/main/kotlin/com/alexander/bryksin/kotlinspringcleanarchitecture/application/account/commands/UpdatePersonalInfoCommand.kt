package com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.commands

import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.valueObjects.AccountId
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.valueObjects.PersonalInfo

data class UpdatePersonalInfoCommand(
    val accountId: AccountId,
    val personalInfo: PersonalInfo
) : AccountDomainCommand {
    companion object {}
}
