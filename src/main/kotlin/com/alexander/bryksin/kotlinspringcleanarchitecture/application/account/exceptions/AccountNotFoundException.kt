package com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.exceptions

data class AccountNotFoundException(val id: Any) : RuntimeException("account with id: $id not found")
