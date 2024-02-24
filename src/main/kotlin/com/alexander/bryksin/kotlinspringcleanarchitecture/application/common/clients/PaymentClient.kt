package com.alexander.bryksin.kotlinspringcleanarchitecture.application.common.clients

interface PaymentClient {
    suspend fun verifyPaymentTransaction(accountId: String, transactionId: String)
}