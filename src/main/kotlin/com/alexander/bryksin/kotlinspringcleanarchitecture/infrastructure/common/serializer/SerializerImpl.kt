package com.alexander.bryksin.kotlinspringcleanarchitecture.infrastructure.common.serializer

import com.alexander.bryksin.kotlinspringcleanarchitecture.application.common.serializer.SerializationException
import com.alexander.bryksin.kotlinspringcleanarchitecture.application.common.serializer.Serializer
import com.fasterxml.jackson.databind.ObjectMapper
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Component


@Component
class SerializerImpl(private val objectMapper: ObjectMapper) : Serializer {

    override fun <T> deserialize(data: ByteArray, clazz: Class<T>): T = try {
        objectMapper.readValue(data, clazz)
    } catch (ex: Exception) {
        log.error { "error while deserializing data: ${ex.message}, class: ${clazz.name}" }
        throw SerializationException(ex, clazz)
    }

    override fun serializeToBytes(data: Any): ByteArray = try {
        objectMapper.writeValueAsBytes(data)
    } catch (ex: Exception) {
        log.error { "error while serializing data: ${ex.message}, class: ${data::class.java.name}" }
        throw SerializationException(ex, data::class.java)
    }

    override fun serializeToString(data: Any): String = try {
        objectMapper.writeValueAsString(data)
    } catch (ex: Exception) {
        log.error { "error while serializing data: ${ex.message}, class: ${data::class.java.name}" }
        throw SerializationException(ex, data::class.java)
    }

    private companion object {
        private val log = KotlinLogging.logger { }
    }
}