package com.alexander.bryksin.kotlinspringcleanarchitecture.api.account.contracts

import com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.commands.DepositBalanceCommand
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.valueObjects.AccountId
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.valueObjects.Balance

data class DepositBalanceRequest(val balance: Balance, val transactionId: String) {
    companion object {}
}

fun DepositBalanceRequest.toCommand(accountId: AccountId) = DepositBalanceCommand(
    accountId = accountId,
    balance = this.balance,
    transactionId = this.transactionId
)