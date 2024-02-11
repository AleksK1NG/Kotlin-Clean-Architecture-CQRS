package com.alexander.bryksin.kotlinspringcleanarchitecture.application.common.publisher

import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.common.outbox.models.OutboxEvent
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Component


@Component
class OutboxPublisherImpl : OutboxPublisher {
    override suspend fun publish(event: OutboxEvent) {
        log.info { "Publishing outbox event: $event" }
    }

    override suspend fun publish(events: List<OutboxEvent>) {
        log.info { "Publishing outbox events: $events" }
    }


    private companion object {
        private val log = KotlinLogging.logger {  }
    }
}