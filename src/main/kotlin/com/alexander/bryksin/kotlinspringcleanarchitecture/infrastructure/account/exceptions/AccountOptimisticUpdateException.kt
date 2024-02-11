package com.alexander.bryksin.kotlinspringcleanarchitecture.infrastructure.account.exceptions

import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.models.Account

data class AccountOptimisticUpdateException(val account: Account) : RuntimeException(
    "error while updating accountId: ${account.accountId?.id} version: ${account.version}"
)
