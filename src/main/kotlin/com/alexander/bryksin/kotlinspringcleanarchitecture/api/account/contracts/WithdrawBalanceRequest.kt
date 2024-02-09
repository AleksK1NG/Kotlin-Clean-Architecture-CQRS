package com.alexander.bryksin.kotlinspringcleanarchitecture.api.account.contracts

import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.valueObjects.Balance

data class WithdrawBalanceRequest(val balance: Balance, val transactionId: String) {
    companion object {}
}
