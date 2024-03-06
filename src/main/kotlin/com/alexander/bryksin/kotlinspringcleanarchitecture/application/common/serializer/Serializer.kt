package com.alexander.bryksin.kotlinspringcleanarchitecture.application.common.serializer

import arrow.core.Either
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.errors.AppError
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.errors.SerializationError

interface Serializer {

    fun <T> deserialize(data: ByteArray, clazz: Class<T>): T

    fun serializeToBytes(data: Any): ByteArray

    fun serializeToString(data: Any): String
}


fun <T> Serializer.deserializeTo(data: ByteArray, clazz: Class<T>): Either<AppError, T> {
    return Either.catch { this.deserialize(data, clazz) }
        .mapLeft { err -> SerializationError("error while deserializing data: ${err.message}, class: ${clazz.name}") }
}