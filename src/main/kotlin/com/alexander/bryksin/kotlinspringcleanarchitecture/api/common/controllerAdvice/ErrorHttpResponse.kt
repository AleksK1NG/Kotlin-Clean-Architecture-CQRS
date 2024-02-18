package com.alexander.bryksin.kotlinspringcleanarchitecture.api.common.controllerAdvice

data class ErrorHttpResponse(
    val status: Int,
    val message: String,
    val timestamp: String
)
