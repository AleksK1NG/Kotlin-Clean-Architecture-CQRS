package com.alexander.bryksin.kotlinspringcleanarchitecture.api.account.contracts

import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.valueObjects.AccountStatus

data class ChangeAccountStatusRequest(val status: AccountStatus) {
    companion object {}
}
