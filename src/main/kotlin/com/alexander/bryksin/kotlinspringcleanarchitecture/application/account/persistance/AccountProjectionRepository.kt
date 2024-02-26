package com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.persistance

import arrow.core.Either
import com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.models.AccountsList
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.errors.AppError
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.models.Account
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.valueObjects.AccountId

interface AccountProjectionRepository {

    suspend fun createAccount(account: Account): Either<AppError, Account>

    suspend fun updateAccount(account: Account): Either<AppError, Account>

    suspend fun getAccountById(id: AccountId):Either<AppError, Account>

    suspend fun getAccountByEmail(email: String): Either<AppError, Account>

    suspend fun getAllAccounts(page: Int, size: Int): Either<AppError, AccountsList>

    suspend fun upsert(account: Account): Either<AppError, Account>
}