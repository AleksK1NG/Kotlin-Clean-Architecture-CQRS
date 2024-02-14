package com.alexander.bryksin.kotlinspringcleanarchitecture.api.common

import com.alexander.bryksin.kotlinspringcleanarchitecture.application.common.serializer.Serializer
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.common.outbox.models.OutboxEvent
import org.apache.kafka.clients.consumer.ConsumerRecord

fun <T> Serializer.deserializeRecordToEvent(consumerRecord: ConsumerRecord<String, ByteArray>, clazz: Class<T>): T {
    val outboxEvent = deserialize(consumerRecord.value(), OutboxEvent::class.java)
    return deserialize(outboxEvent.data, clazz)
}