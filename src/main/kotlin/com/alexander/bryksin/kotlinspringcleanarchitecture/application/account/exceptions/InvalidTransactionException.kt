package com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.exceptions

import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.valueObjects.AccountId

data class InvalidTransactionException(val transactionId: String, val accountId: AccountId) : RuntimeException(
    "transaction: $transactionId is invalid, accountId $accountId "
)