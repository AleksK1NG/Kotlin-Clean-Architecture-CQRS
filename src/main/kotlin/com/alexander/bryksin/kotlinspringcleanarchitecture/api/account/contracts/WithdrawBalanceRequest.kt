package com.alexander.bryksin.kotlinspringcleanarchitecture.api.account.contracts

import com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.commands.WithdrawBalanceCommand
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.valueObjects.AccountId
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.valueObjects.Balance

data class WithdrawBalanceRequest(val balance: Balance, val transactionId: String) {
    companion object {}
}

fun WithdrawBalanceRequest.toCommand(accountId: AccountId) = WithdrawBalanceCommand(
    accountId = accountId,
    balance = this.balance,
    transactionId = this.transactionId
)