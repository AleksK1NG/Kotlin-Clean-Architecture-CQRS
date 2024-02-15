package com.alexander.bryksin.kotlinspringcleanarchitecture.api.account.contracts

import com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.commands.UpdatePersonalInfoCommand
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.valueObjects.AccountId
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.valueObjects.PersonalInfo

data class UpdatePersonalInfoRequest(val personalInfo: PersonalInfo) {
    companion object {}
}

fun UpdatePersonalInfoRequest.toCommand(accountId: AccountId) = UpdatePersonalInfoCommand(
    accountId = accountId,
    personalInfo = personalInfo
)