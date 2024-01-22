package com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.valueObjects

import java.util.*

data class AccountId(val id: UUID) {
    fun string() = id.toString()
}
