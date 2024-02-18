package com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.exceptions

import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.valueObjects.AccountId

data class InvalidVersionException(val accountId: AccountId?, val version: Long)
    : RuntimeException("invalid version for id: ${accountId?.id} version: $version")
