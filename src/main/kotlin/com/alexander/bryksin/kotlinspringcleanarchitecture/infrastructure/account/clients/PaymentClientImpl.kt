package com.alexander.bryksin.kotlinspringcleanarchitecture.infrastructure.account.clients

import com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.exceptions.InvalidTransactionException
import com.alexander.bryksin.kotlinspringcleanarchitecture.application.common.clients.PaymentClient
import org.springframework.stereotype.Component


/*
Example implementation for demonstration purposes
 */
@Component
class PaymentClientImpl : PaymentClient {

    override suspend fun verifyPaymentTransaction(accountId: String, transactionId: String) {
        if (accountId.isBlank() || transactionId.isBlank())
            throw InvalidTransactionException(transactionId, accountId)
    }
}