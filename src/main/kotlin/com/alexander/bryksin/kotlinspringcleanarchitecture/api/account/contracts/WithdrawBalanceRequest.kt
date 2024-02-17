package com.alexander.bryksin.kotlinspringcleanarchitecture.api.account.contracts

import com.alexander.bryksin.kotlinspringcleanarchitecture.api.account.contracts.dto.BalanceRequest
import com.alexander.bryksin.kotlinspringcleanarchitecture.api.account.contracts.dto.toBalance
import com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.commands.WithdrawBalanceCommand
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.valueObjects.AccountId

data class WithdrawBalanceRequest(val balance: BalanceRequest, val transactionId: String) {
    companion object {}
}

fun WithdrawBalanceRequest.toCommand(accountId: AccountId) = WithdrawBalanceCommand(
    accountId = accountId,
    balance = this.balance.toBalance(),
    transactionId = this.transactionId
)