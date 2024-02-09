package com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.commands

import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.valueObjects.AccountId
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.valueObjects.AccountStatus

data class ChangeAccountStatusCommand(val accountId: AccountId, val status: AccountStatus) {
    companion object {}
}
