package com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.exceptions

data class EmailVerificationException(val email: String) : RuntimeException("email $email is not verified")