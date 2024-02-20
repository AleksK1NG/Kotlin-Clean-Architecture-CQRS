package com.alexander.bryksin.kotlinspringcleanarchitecture.infrastructure.account.clients

import com.alexander.bryksin.kotlinspringcleanarchitecture.application.common.clients.PaymentClient
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Component

@Component
class PaymentClientImpl : PaymentClient {

    override suspend fun verifyPaymentTransaction(accountId: String, transactionId: String): Boolean {
       return accountId.isNotEmpty() && transactionId.isNotEmpty()
    }

    private companion object {
        private val log = KotlinLogging.logger { }
    }
}