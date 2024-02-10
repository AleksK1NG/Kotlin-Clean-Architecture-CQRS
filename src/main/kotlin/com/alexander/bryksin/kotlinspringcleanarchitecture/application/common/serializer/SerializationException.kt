package com.alexander.bryksin.kotlinspringcleanarchitecture.application.common.serializer

data class SerializationException(val ex: Throwable, val clazz: Class<*>) :
    RuntimeException("error while serializing $clazz, error: ${ex.message}")
