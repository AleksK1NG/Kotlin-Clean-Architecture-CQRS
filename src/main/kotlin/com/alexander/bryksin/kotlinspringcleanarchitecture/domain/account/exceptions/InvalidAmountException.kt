package com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.exceptions

data class InvalidAmountException(val id: Any, val amount: Any) :
    RuntimeException("invalid amount: $amount for account: $id")
