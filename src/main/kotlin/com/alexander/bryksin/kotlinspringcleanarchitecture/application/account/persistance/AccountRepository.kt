package com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.persistance

import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.models.Account
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.valueObjects.AccountId

interface AccountRepository {
    suspend fun createAccount(account: Account): Account
    suspend fun updateAccount(account: Account): Account
    suspend fun getAccountById(id: AccountId): Account?
}