package com.alexander.bryksin.kotlinspringcleanarchitecture.api.account.contracts

import com.alexander.bryksin.kotlinspringcleanarchitecture.api.account.contracts.dto.PersonalInfoRequest
import com.alexander.bryksin.kotlinspringcleanarchitecture.api.account.contracts.dto.toPersonalInfo
import com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.commands.UpdatePersonalInfoCommand
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.valueObjects.AccountId
import jakarta.validation.Valid

data class UpdatePersonalInfoRequest(@field:Valid val personalInfo: PersonalInfoRequest) {
    companion object {}
}

fun UpdatePersonalInfoRequest.toCommand(accountId: AccountId) = UpdatePersonalInfoCommand(
    accountId = accountId,
    personalInfo = personalInfo.toPersonalInfo()
)