package com.alexander.bryksin.kotlinspringcleanarchitecture.infrastructure.account.clients

import com.alexander.bryksin.kotlinspringcleanarchitecture.application.common.clients.EmailVerifierClient
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.exceptions.EmailVerificationException
import kotlinx.coroutines.delay
import org.springframework.stereotype.Component

@Component
class EmailVerifierClientImpl : EmailVerifierClient {

    override suspend fun verifyEmail(email: String) {
        delay(300)
        if (email.isBlank()) throw EmailVerificationException(email)
    }
}