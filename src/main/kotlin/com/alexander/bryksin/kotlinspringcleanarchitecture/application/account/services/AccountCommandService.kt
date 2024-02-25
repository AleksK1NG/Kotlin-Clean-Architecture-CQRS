package com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.services

import arrow.core.Either
import com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.commands.*
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.errors.AppError
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.valueObjects.AccountId

interface AccountCommandService {

    suspend fun handle(command: CreateAccountCommand): Either<AppError, AccountId>

    suspend fun handle(command: ChangeAccountStatusCommand): Either<AppError, Unit>

    suspend fun handle(command: ChangeContactInfoCommand): Either<AppError, Unit>

    suspend fun handle(command: DepositBalanceCommand): Either<AppError, Unit>

    suspend fun handle(command: WithdrawBalanceCommand): Either<AppError, Unit>

    suspend fun handle(command: UpdatePersonalInfoCommand): Either<AppError, Unit>
}