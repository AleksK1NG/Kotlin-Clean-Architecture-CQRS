package com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.persistance

import arrow.core.Either
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.errors.AppError
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.models.Account
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.valueObjects.AccountId

interface AccountRepository {

    suspend fun getAccountById(id: AccountId): Either<AppError, Account>

    suspend fun saveAccount(account: Account): Either<AppError, Account>

    suspend fun updateAccount(account: Account): Either<AppError, Account>
}