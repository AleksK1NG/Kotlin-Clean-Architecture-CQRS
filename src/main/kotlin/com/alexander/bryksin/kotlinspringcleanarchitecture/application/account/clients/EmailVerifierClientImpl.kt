package com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.clients

import com.alexander.bryksin.kotlinspringcleanarchitecture.infrastructure.account.clients.EmailVerifierClient
import io.github.oshai.kotlinlogging.KotlinLogging

class EmailVerifierClientImpl : EmailVerifierClient {

    override suspend fun verifyEmail(email: String): Boolean {
        return email.isBlank()
    }

    private companion object {
        private val log = KotlinLogging.logger { }
    }
}