package com.alexander.bryksin.kotlinspringcleanarchitecture.infrastructure.account.persistance

import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.models.Account
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.valueObjects.AccountId

interface AccountProjectionRepository {
    suspend fun createAccount(account: Account): Account
    suspend fun updateAccount(id: AccountId, account: Account): Account
    suspend fun getAccountById(id: AccountId): Account
}