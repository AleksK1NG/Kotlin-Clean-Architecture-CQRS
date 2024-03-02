package com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.valueObjects

import java.util.*

@JvmInline
value class AccountId(val id: UUID? = null) {
    fun string() = id?.toString() ?: ""
}
