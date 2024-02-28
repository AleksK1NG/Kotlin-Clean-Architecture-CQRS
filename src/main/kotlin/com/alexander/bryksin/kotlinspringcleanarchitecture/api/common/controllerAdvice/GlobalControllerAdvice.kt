package com.alexander.bryksin.kotlinspringcleanarchitecture.api.common.controllerAdvice

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.dao.DuplicateKeyException
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.support.WebExchangeBindException
import org.springframework.web.server.ServerWebInputException
import java.time.Instant
import javax.security.auth.login.AccountNotFoundException


@ControllerAdvice
class GlobalControllerAdvice {

    @ExceptionHandler(value = [RuntimeException::class])
    fun handleRuntimeException(ex: RuntimeException, request: ServerHttpRequest): ResponseEntity<ErrorHttpResponse> {
        val errorHttpResponse = ErrorHttpResponse(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            ex.message ?: "",
            Instant.now().toString()
        )

        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .contentType(MediaType.APPLICATION_JSON)
            .body(errorHttpResponse)
            .also { log.error { "(GlobalControllerAdvice) ${ex::class.java.name}: ${ex.message}" } }
    }

    @ExceptionHandler(value = [DuplicateKeyException::class])
    fun handleDuplicateKeyException(
        ex: DuplicateKeyException,
        request: ServerHttpRequest
    ): ResponseEntity<ErrorHttpResponse> {
        val errorHttpResponse = ErrorHttpResponse(
            HttpStatus.BAD_REQUEST.value(),
            ex.message ?: "",
            Instant.now().toString()
        )

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .contentType(MediaType.APPLICATION_JSON)
            .body(errorHttpResponse)
            .also { log.warn { "(GlobalControllerAdvice) ${ex::class.java.name}: ${ex.message}" } }
    }

    @ExceptionHandler(value = [AccountNotFoundException::class])
    fun handleAccountNotFoundException(
        ex: AccountNotFoundException,
        request: ServerHttpRequest
    ): ResponseEntity<ErrorHttpResponse> {
        val errorHttpResponse = ErrorHttpResponse(
            HttpStatus.NOT_FOUND.value(),
            ex.message ?: "",
            Instant.now().toString()
        )

        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .contentType(MediaType.APPLICATION_JSON)
            .body(errorHttpResponse)
            .also { log.warn { "(GlobalControllerAdvice) ${ex::class.java.name}: ${ex.message}" } }
    }

    @ExceptionHandler(value = [ServerWebInputException::class])
    fun handleServerWebInputException(
        ex: ServerWebInputException,
        request: ServerHttpRequest
    ): ResponseEntity<ErrorHttpResponse> {
        val errorHttpResponse = ErrorHttpResponse(
            status = HttpStatus.BAD_REQUEST.value(),
            message = ex.message,
            timestamp = Instant.now().toString()
        )

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .contentType(MediaType.APPLICATION_JSON)
            .body(errorHttpResponse)
            .also { log.warn { "(GlobalControllerAdvice) ${ex::class.java.name}: ${ex.message}" } }
    }


    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(value = [WebExchangeBindException::class])
    fun handleWebExchangeInvalidArgument(ex: WebExchangeBindException): ResponseEntity<MutableMap<String, Any>> {
        val errorMap = mutableMapOf<String, Any>()
        ex.bindingResult.fieldErrors.forEach { error ->
            error.defaultMessage?.let {
                errorMap[error.field] = mapOf(
                    "reason" to it,
                    "rejectedValue" to error.rejectedValue,
                )
            }
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).contentType(MediaType.APPLICATION_JSON).body(errorMap)
            .also { log.warn { "(GlobalControllerAdvice) ${ex::class.java.name}: ${ex.message}" } }
    }

    private companion object {
        private val log = KotlinLogging.logger { }
    }
}