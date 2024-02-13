package com.alexander.bryksin.kotlinspringcleanarchitecture.api.configuration.kafka

import org.apache.kafka.clients.admin.AdminClientConfig
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.core.KafkaAdmin

@Configuration
class KafkaAdminConfiguration(
    @Value(value = "\${spring.kafka.bootstrap-servers:localhost:9092}")
    private val bootstrapServers: String,
    private val kafkaTopics: KafkaTopics,
) {

    @Bean
    fun kafkaAdmin(): KafkaAdmin {
        val configs: MutableMap<String, Any> = hashMapOf(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers)
        return KafkaAdmin(configs)
    }
}