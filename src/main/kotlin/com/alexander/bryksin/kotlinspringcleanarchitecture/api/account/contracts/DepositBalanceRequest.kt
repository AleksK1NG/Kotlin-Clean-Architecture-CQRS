package com.alexander.bryksin.kotlinspringcleanarchitecture.api.account.contracts

import com.alexander.bryksin.kotlinspringcleanarchitecture.api.account.contracts.dto.BalanceRequest
import com.alexander.bryksin.kotlinspringcleanarchitecture.api.account.contracts.dto.toBalance
import com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.commands.DepositBalanceCommand
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.valueObjects.AccountId
import jakarta.validation.constraints.Size

data class DepositBalanceRequest(
    val balance: BalanceRequest,
    @field:Size(min = 6, max = 255) val transactionId: String
) {
    companion object
}

fun DepositBalanceRequest.toCommand(accountId: AccountId) = DepositBalanceCommand(
    accountId = accountId,
    balance = this.balance.toBalance(),
    transactionId = this.transactionId
)