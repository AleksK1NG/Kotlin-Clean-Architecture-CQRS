package com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.commands

import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.valueObjects.Balance

data class WithdrawBalanceCommand(val accountId: String, val balance: Balance, val transactionId: String) {
    companion object {}
}
