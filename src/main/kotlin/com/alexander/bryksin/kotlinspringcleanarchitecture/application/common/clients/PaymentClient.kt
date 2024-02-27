package com.alexander.bryksin.kotlinspringcleanarchitecture.application.common.clients

import arrow.core.Either
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.errors.AppError

interface PaymentClient {
    suspend fun verifyPaymentTransaction(accountId: String, transactionId: String): Either<AppError, Unit>
}