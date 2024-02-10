package com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.clients

import com.alexander.bryksin.kotlinspringcleanarchitecture.infrastructure.account.clients.EmailVerifierClient
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.delay
import org.springframework.stereotype.Component

@Component
class EmailVerifierClientImpl : EmailVerifierClient {

    override suspend fun verifyEmail(email: String): Boolean {
        delay(300)
        log.info { "email verified: $email" }
        return email.isBlank() && email.contains("@")
    }

    private companion object {
        private val log = KotlinLogging.logger { }
    }
}