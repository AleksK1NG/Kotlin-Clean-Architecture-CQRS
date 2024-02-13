package com.alexander.bryksin.kotlinspringcleanarchitecture.api.configuration.kafka

import io.github.oshai.kotlinlogging.KotlinLogging
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.ByteArrayDeserializer
import org.apache.kafka.common.serialization.StringDeserializer
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.listener.ContainerProperties

@Configuration
class KafkaConsumerConfiguration(
    @Value(value = "\${spring.kafka.bootstrap-servers:localhost:9092}")
    private val bootstrapServers: String,

    @Value(value = "\${spring.kafka.consumer.group-id:order-microservice-group-id}")
    private val groupId: String,
) {

    @Bean
    fun kafkaListenerContainerFactory(consumerFactory: ConsumerFactory<String, ByteArray>): ConcurrentKafkaListenerContainerFactory<String, ByteArray> =
        ConcurrentKafkaListenerContainerFactory<String, ByteArray>().apply {
            this.consumerFactory = consumerFactory
            setConcurrency(Runtime.getRuntime().availableProcessors())
            this.containerProperties.ackMode = ContainerProperties.AckMode.MANUAL
            this.containerProperties.isMicrometerEnabled = true
            this.containerProperties.isObservationEnabled = true
        }


    @Bean
    fun consumerFactory(): ConsumerFactory<String, ByteArray> = DefaultKafkaConsumerFactory(consumerProps())

    private fun consumerProps(): Map<String, Any> = hashMapOf<String, Any>(
        ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers,
        ConsumerConfig.GROUP_ID_CONFIG to groupId,
        ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
        ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to ByteArrayDeserializer::class.java,
        ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG to "false"
    )

    companion object {
        private val log = KotlinLogging.logger { }
    }
}