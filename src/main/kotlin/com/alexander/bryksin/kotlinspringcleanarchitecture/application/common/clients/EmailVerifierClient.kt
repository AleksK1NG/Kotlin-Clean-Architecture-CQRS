package com.alexander.bryksin.kotlinspringcleanarchitecture.application.common.clients

import arrow.core.Either
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.errors.AppError

interface EmailVerifierClient {
    suspend fun verifyEmail(email: String): Either<AppError, Unit>
}