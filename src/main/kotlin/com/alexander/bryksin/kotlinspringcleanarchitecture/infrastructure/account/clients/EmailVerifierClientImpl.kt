package com.alexander.bryksin.kotlinspringcleanarchitecture.infrastructure.account.clients

import arrow.core.Either
import arrow.core.raise.either
import com.alexander.bryksin.kotlinspringcleanarchitecture.application.common.clients.EmailVerifierClient
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.errors.AppError
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.errors.EmailValidationError
import kotlinx.coroutines.delay
import org.springframework.stereotype.Component

/*
Example implementation for demonstration purposes
 */
@Component
class EmailVerifierClientImpl : EmailVerifierClient {

    override suspend fun verifyEmail(email: String): Either<AppError, Unit> = either {
        delay(300)
        if (email.isBlank()) raise(EmailValidationError("invalid email: $email"))
    }
}