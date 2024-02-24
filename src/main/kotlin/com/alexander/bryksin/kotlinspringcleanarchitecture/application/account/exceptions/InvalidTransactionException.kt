package com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.exceptions

data class InvalidTransactionException(val transactionId: String, val accountId: Any) : RuntimeException(
    "transaction: $transactionId is invalid, accountId $accountId "
)