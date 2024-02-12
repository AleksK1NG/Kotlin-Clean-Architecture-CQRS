package com.alexander.bryksin.kotlinspringcleanarchitecture.infrastructure.schedulers

import com.alexander.bryksin.kotlinspringcleanarchitecture.application.common.outbox.persistance.OutboxRepository
import com.alexander.bryksin.kotlinspringcleanarchitecture.application.common.publisher.OutboxPublisher
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.runBlocking
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component


@Component
@ConditionalOnProperty(prefix = "schedulers", value = ["outbox.enable"], havingValue = "true")
class OutboxScheduler(
    private val outboxRepository: OutboxRepository,
    private val publisher: OutboxPublisher,
) {

    @Value("\${scheduler.outbox.batchSize}")
    private var batchSize: Int = 10

    @Scheduled(
        initialDelayString = "\${schedulers.outbox.initialDelayMillis}",
        fixedRateString = "\${schedulers.outbox.fixedRate}"
    )
    fun publishOutboxEvents() = runBlocking {
        try {
            outboxRepository.deleteEventsWithLock(batchSize) { publisher.publish(it) }
        } catch (e: Exception) {
            log.error { "error while publishing outbox events: ${e.message}" }
        }
    }

    private companion object {
        private val log = KotlinLogging.logger {}
    }
}