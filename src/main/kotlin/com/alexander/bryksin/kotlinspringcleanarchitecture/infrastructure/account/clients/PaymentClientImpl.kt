package com.alexander.bryksin.kotlinspringcleanarchitecture.infrastructure.account.clients

import com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.exceptions.InvalidTransactionException
import com.alexander.bryksin.kotlinspringcleanarchitecture.application.common.clients.PaymentClient
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Component

@Component
class PaymentClientImpl : PaymentClient {

    override suspend fun verifyPaymentTransaction(accountId: String, transactionId: String)  {
      if (accountId.isBlank() || transactionId.isBlank()) throw  throw InvalidTransactionException(transactionId, accountId)
    }

    private companion object {
        private val log = KotlinLogging.logger { }
    }
}