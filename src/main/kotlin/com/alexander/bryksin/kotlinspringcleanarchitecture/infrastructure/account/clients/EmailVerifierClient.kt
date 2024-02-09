package com.alexander.bryksin.kotlinspringcleanarchitecture.infrastructure.account.clients

interface EmailVerifierClient {
    suspend fun verifyEmail(email: String): Boolean
}