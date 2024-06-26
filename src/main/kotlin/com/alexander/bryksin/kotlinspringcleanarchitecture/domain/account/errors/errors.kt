package com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.errors

sealed interface AppError


data class AccountNotFoundError(val msg: String) : AppError
data class EmailValidationError(val msg: String) : AppError
data class PaymentValidationError(val msg: String) : AppError
data class InvalidBalanceError(val msg: String) : AppError
data class InvalidBalanceCurrency(val msg: String) : AppError
data class InvalidBalanceAmount(val msg: String) : AppError
data class InvalidVersion(val msg: String) : AppError
data class LowerEventVersionError(val id: Any?, val expectedVersion: Any, val eventVersion: Any) : AppError
data class SameEventVersionError(val id: Any?, val expectedVersion: Any, val eventVersion: Any) : AppError
data class UpperEventVersionError(val id: Any?, val expectedVersion: Any, val eventVersion: Any) : AppError
data class InvalidTransactionError(val msg: String, val transactionId: String = "") : AppError
data class GenericAppError(
    val msg: String,
    val cause: Throwable? = null,
    val exceptionClazz: Class<*>? = null
) : AppError

data class SerializationError(val msg: String) : AppError