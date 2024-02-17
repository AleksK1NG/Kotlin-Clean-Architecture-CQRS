package com.alexander.bryksin.kotlinspringcleanarchitecture.api.account.contracts.dto

import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.valueObjects.Balance
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.valueObjects.BalanceCurrency
import jakarta.validation.constraints.Min

data class BalanceRequest(
    @field:Min(1) val amount: Long = 0,
    val balanceCurrency: BalanceCurrency = BalanceCurrency.USD
) {
    companion object
}


fun BalanceRequest.toBalance() = Balance(
    amount = amount,
    balanceCurrency = balanceCurrency
)