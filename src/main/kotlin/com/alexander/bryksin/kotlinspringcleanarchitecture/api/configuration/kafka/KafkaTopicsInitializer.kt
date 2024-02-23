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
        kotlin.runCatching {
            kafkaTopics.getTopics()
                .map { NewTopic(it.name, it.partitions, it.replication.toShort()) }
                .forEach {
                    kafkaAdmin.createOrModifyTopics(it)
                    log.info { "created or modified topic: $it" }
                }
        }
            .onSuccess { log.info { "kafka topics created" } }
            .onFailure { log.error { "error while creating kafka topics: ${it.message}" } }
            .getOrThrow()
    }

    private companion object {
        private val log = KotlinLogging.logger { }
    }
}