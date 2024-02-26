package com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.services

import arrow.core.Either
import com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.events.*
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.errors.AppError

interface AccountEventHandlerService {

    suspend fun on(event: AccountCreatedEvent): Either<AppError, Unit>

    suspend fun on(event: BalanceDepositedEvent): Either<AppError, Unit>

    suspend fun on(event: BalanceWithdrawEvent): Either<AppError, Unit>

    suspend fun on(event: PersonalInfoUpdatedEvent): Either<AppError, Unit>

    suspend fun on(event: ContactInfoChangedEvent): Either<AppError, Unit>

    suspend fun on(event: AccountStatusChangedEvent): Either<AppError, Unit>
}