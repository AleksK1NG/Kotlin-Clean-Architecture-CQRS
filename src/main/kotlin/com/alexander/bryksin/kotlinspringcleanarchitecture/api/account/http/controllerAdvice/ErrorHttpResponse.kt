package com.alexander.bryksin.kotlinspringcleanarchitecture.api.account.http.controllerAdvice

data class ErrorHttpResponse(
    val status: Int,
    val message: String,
    val timestamp: String
)
