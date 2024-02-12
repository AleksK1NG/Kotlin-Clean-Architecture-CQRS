package com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.persistance

import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.models.Account
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.valueObjects.AccountId

interface AccountRepository {
    suspend fun getAccountById(id: AccountId): Account?
    suspend fun saveAccount(account: Account): Account
    suspend fun updateAccount(account: Account): Account
}