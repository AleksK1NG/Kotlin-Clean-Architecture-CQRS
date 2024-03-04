package com.alexander.bryksin.kotlinspringcleanarchitecture.api.account.http.contracts

import com.alexander.bryksin.kotlinspringcleanarchitecture.api.account.http.contracts.dto.BalanceRequest
import com.alexander.bryksin.kotlinspringcleanarchitecture.api.account.http.contracts.dto.toBalance
import com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.commands.WithdrawBalanceCommand
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.valueObjects.AccountId
import jakarta.validation.Valid
import jakarta.validation.constraints.Size

data class WithdrawBalanceRequest(
    @field:Valid val balance: BalanceRequest,
    @field:Size(min = 6, max = 255) val transactionId: String
) {
    companion object
}

fun WithdrawBalanceRequest.toCommand(accountId: AccountId) = WithdrawBalanceCommand(
    accountId = accountId,
    balance = this.balance.toBalance(),
    transactionId = this.transactionId
)