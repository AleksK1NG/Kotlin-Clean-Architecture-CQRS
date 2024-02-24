package com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.persistance

import com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.models.AccountsList
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.models.Account
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.valueObjects.AccountId

interface AccountProjectionRepository {

    suspend fun createAccount(account: Account): Account

    suspend fun updateAccount(account: Account): Account

    suspend fun getAccountById(id: AccountId): Account?

    suspend fun getAccountByEmail(email: String): Account?

    suspend fun getAllAccounts(page: Int, size: Int): AccountsList
}