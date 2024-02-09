package com.alexander.bryksin.kotlinspringcleanarchitecture.infrastructure.account.clients

interface PaymentClient {
    suspend fun verifyPaymentTransaction(accountId: String, transactionId: String): Boolean
}