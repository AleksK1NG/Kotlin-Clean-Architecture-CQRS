package com.alexander.bryksin.kotlinspringcleanarchitecture.application.common.serializer

interface Serializer {

    fun <T> deserialize(data: ByteArray, clazz: Class<T>): T

    fun serializeToBytes(data: Any): ByteArray

    fun serializeToString(data: Any): String
}


inline fun <reified T> Serializer.deserializeTo(data: ByteArray) = this.deserialize(data, T::class.java)