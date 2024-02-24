package com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.models

import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.models.Account

data class AccountsList(
    val page: Int,
    val size: Int,
    val totalCount: Int,
    val accountsList: List<Account>
) {
    companion object
}
