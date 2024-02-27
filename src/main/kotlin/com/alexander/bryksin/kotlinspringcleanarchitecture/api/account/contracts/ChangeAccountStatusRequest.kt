package com.alexander.bryksin.kotlinspringcleanarchitecture.api.account.contracts

import com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.commands.ChangeAccountStatusCommand
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.valueObjects.AccountId
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.valueObjects.AccountStatus

data class ChangeAccountStatusRequest(val status: AccountStatus) {
    companion object
}

fun ChangeAccountStatusRequest.toCommand(accountId: AccountId) = ChangeAccountStatusCommand(
    accountId = accountId,
    status = this.status,
)