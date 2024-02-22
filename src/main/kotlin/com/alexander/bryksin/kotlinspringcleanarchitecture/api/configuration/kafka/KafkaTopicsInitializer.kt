package com.alexander.bryksin.kotlinspringcleanarchitecture.api.configuration.kafka

import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.annotation.PostConstruct
import org.apache.kafka.clients.admin.NewTopic
import org.springframework.kafka.core.KafkaAdmin
import org.springframework.stereotype.Component


@Component
class KafkaTopicsInitializer(
    private val kafkaTopics: KafkaTopics,
    private val kafkaAdmin: KafkaAdmin
) {

    @PostConstruct
    fun init() {
        try {
            kafkaTopics.getTopics()
                .map { NewTopic(it.name, it.partitions, it.replication.toShort()) }
                .forEach {
                    kafkaAdmin.createOrModifyTopics(it)
                    log.info { "created or modified topic: $it" }
                }
        } catch (e: Exception) {
            log.error { "error while initializing kafka topics: ${e.message}" }
            throw e
        }
    }


    private companion object {
        private val log = KotlinLogging.logger { }
    }
}