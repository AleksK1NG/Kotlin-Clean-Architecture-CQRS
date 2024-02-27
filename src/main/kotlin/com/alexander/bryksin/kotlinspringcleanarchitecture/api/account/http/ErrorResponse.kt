package com.alexander.bryksin.kotlinspringcleanarchitecture.api.account.http

import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.errors.*
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity

internal fun mapErrorToResponse(err: AppError): ResponseEntity<*> {
    return when (err) {
        is GenericAppError -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(err.msg)
        is AccountNotFoundError -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(err.msg)
        is EmailValidationError -> ResponseEntity.status(HttpStatus.BAD_REQUEST).body(err.msg)
        is InvalidBalanceError -> ResponseEntity.status(HttpStatus.BAD_REQUEST).body(err.msg)
        is InvalidVersion -> ResponseEntity.status(HttpStatus.BAD_REQUEST).body(err.msg)
        is PaymentValidationError -> ResponseEntity.status(HttpStatus.BAD_REQUEST).body(err.msg)
        is LowerEventVersionError -> ResponseEntity.status(HttpStatus.BAD_REQUEST).body(err.toString())
        is SameEventVersionError -> ResponseEntity.status(HttpStatus.BAD_REQUEST).body(err.toString())
        is UpperEventVersionError -> ResponseEntity.status(HttpStatus.BAD_REQUEST).body(err.toString())
        is InvalidTransactionError -> ResponseEntity.status(HttpStatus.BAD_REQUEST).body(err.msg)
    }
}