package com.alexander.bryksin.kotlinspringcleanarchitecture.api.common.kafkaUtils

import com.alexander.bryksin.kotlinspringcleanarchitecture.api.account.kafka.EventProcessor
import com.alexander.bryksin.kotlinspringcleanarchitecture.api.account.kafka.EventProcessor.Companion.KAFKA_HEADERS_ERROR_MESSAGE
import com.alexander.bryksin.kotlinspringcleanarchitecture.application.common.serializer.Serializer
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.common.outbox.models.OutboxEvent
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.header.Headers


fun ConsumerRecord<String, ByteArray>.info(withValue: Boolean = true): String {
    val value = String(value())
    return if (withValue)
        "topic: ${topic()} key: ${key()} partition: ${partition()} offset: ${offset()} timestamp: ${timestamp()} value: $value"
    else
        "topic: ${topic()} key: ${key()} partition: ${partition()} offset: ${offset()} timestamp: ${timestamp()}"
}

fun ConsumerRecord<String, ByteArray>.getRetriesCount(): Result<Int> = runCatching {
    String(headers().lastHeader(EventProcessor.KAFKA_HEADERS_RETRY).value()).toInt()
}

fun ConsumerRecord<String, ByteArray>.retryCount(): Int = runCatching {
    String(headers().lastHeader(EventProcessor.KAFKA_HEADERS_RETRY).value()).toInt()
}.getOrDefault(0)


fun ConsumerRecord<String, ByteArray>.headersWithRetryCount(count: Int = 1): Headers = headers()
    .remove(EventProcessor.KAFKA_HEADERS_RETRY)
    .add(EventProcessor.KAFKA_HEADERS_RETRY, count.toString().toByteArray(Charsets.UTF_8))

fun ConsumerRecord<String, ByteArray>.headersToMap(): MutableMap<String, ByteArray> {
    val headersMap = mutableMapOf<String, ByteArray>()
    headers().forEach { headersMap[it.key()] = it.value() }
    return headersMap
}

fun ConsumerRecord<String, ByteArray>.mergeHeaders(headers: Map<String, ByteArray>): MutableMap<String, ByteArray> {
    val headersMap = mutableMapOf<String, ByteArray>()
    headers().forEach { headersMap[it.key()] = it.value() }
    headers.forEach { headersMap[it.key] = it.value }
    return headersMap
}

fun buildRetryCountHeader(count: Int): MutableMap<String, ByteArray> {
    return mutableMapOf(EventProcessor.KAFKA_HEADERS_RETRY to count.toString().toByteArray(Charsets.UTF_8))
}

fun <T> Serializer.deserializeRecordToEvent(consumerRecord: ConsumerRecord<String, ByteArray>, clazz: Class<T>): T {
    val outboxEvent = deserialize(consumerRecord.value(), OutboxEvent::class.java)
    return deserialize(outboxEvent.data, clazz)
}


fun ConsumerRecord<String, ByteArray>.getKafkaRetryHeaders(
    baseRetryCount: Int = 0,
    step: Int = 1,
    err: Any? = null
): MutableMap<String, ByteArray> {
    val retryCount = getRetriesCount().getOrDefault(baseRetryCount)
    val retryHeadersMap = buildRetryCountHeader(retryCount + step)
    if (err != null) {
        retryHeadersMap[KAFKA_HEADERS_ERROR_MESSAGE] = err.toString().toByteArray(Charsets.UTF_8)
    }
    return retryHeadersMap
}

