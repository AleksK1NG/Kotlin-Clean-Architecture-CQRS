package com.alexander.bryksin.kotlinspringcleanarchitecture.infrastructure.account.clients

import arrow.core.Either
import arrow.core.raise.either
import com.alexander.bryksin.kotlinspringcleanarchitecture.application.common.clients.PaymentClient
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.errors.AppError
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.errors.InvalidTransactionError
import org.springframework.stereotype.Component


/*
Example implementation for demonstration purposes
 */
@Component
class PaymentClientImpl : PaymentClient {

    override suspend fun verifyPaymentTransaction(
        accountId: String,
        transactionId: String
    ): Either<AppError, Unit> = either {
        if (accountId.isBlank() || transactionId.isBlank())
            raise(InvalidTransactionError("invalid transaction id: $transactionId, accountId: $accountId"))
    }
}