package com.alexander.bryksin.kotlinspringcleanarchitecture.infrastructure.configuration.kafka

import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.ByteArraySerializer
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.ProducerFactory

@Configuration
class KafkaProducerConfiguration(
    @Value(value = "\${spring.kafka.bootstrap-servers:localhost:9092}")
    private val bootstrapServers: String,
) {

    private fun senderProps(): Map<String, Any> = hashMapOf<String, Any>(
        ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers,
        ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
        ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to ByteArraySerializer::class.java,
        ProducerConfig.ACKS_CONFIG to ACKS_CONFIG,
        ProducerConfig.RETRIES_CONFIG to RETRIES_CONFIG,
        ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG to DELIVERY_TIMEOUT_MS_CONFIG,
        ProducerConfig.MAX_REQUEST_SIZE_CONFIG to MAX_REQUEST_SIZE_CONFIG,
        ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG to REQUEST_TIMEOUT_MS_CONFIG,
    )

    @Bean
    fun producerFactory(): ProducerFactory<String, ByteArray> = DefaultKafkaProducerFactory(senderProps())

    @Bean
    fun kafkaTemplate(producerFactory: ProducerFactory<String, ByteArray>): KafkaTemplate<String, ByteArray> =
        KafkaTemplate(producerFactory).apply {
            setObservationEnabled(true)
            setMicrometerEnabled(true)
        }

    private companion object {
        private const val ACKS_CONFIG = "all"
        private const val RETRIES_CONFIG = 5
        private const val DELIVERY_TIMEOUT_MS_CONFIG = 120000
        private const val MAX_REQUEST_SIZE_CONFIG = 10685765
        private const val REQUEST_TIMEOUT_MS_CONFIG = 30000
    }
}