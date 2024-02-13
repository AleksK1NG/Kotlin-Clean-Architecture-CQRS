package com.alexander.bryksin.kotlinspringcleanarchitecture.api.configuration.kafka

import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.annotation.PostConstruct
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "topics")
class KafkaTopics {

    var accountCreated: KafkaTopicData = KafkaTopicData()
    var accountCreatedRetry: KafkaTopicData = KafkaTopicData()
    var deadLetterQueue: KafkaTopicData = KafkaTopicData()


    fun getTopics() = listOf(
        accountCreated,
        accountCreatedRetry,
        deadLetterQueue
    )

    @PostConstruct
    fun logConfigProperties() {
        log.info { "configured kafka topics: ${getTopics()}" }
    }

    companion object {
        private val log = KotlinLogging.logger { }
    }
}