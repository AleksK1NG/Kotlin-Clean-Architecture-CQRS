package com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.exceptions

import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.valueObjects.BalanceCurrency

data class InvalidCurrencyException(
    val accountCurrency: BalanceCurrency,
    val transactionCurrency: BalanceCurrency
) : RuntimeException("invalid currency type, account currency: $accountCurrency transaction currency: $transactionCurrency")
