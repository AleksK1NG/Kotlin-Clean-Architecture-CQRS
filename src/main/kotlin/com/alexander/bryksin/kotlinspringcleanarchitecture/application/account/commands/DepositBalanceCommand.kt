package com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.commands

import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.valueObjects.Balance

data class DepositBalanceCommand(val accountId: String, val balance: Balance) {
    companion object {}
}
