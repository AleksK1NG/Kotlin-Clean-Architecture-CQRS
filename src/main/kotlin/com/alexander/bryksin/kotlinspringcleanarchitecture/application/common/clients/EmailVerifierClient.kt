package com.alexander.bryksin.kotlinspringcleanarchitecture.application.common.clients

interface EmailVerifierClient {
    suspend fun verifyEmail(email: String)
}