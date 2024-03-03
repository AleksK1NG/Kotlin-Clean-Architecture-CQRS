package com.alexander.bryksin.kotlinspringcleanarchitecture.infrastructure.schedulers

import com.alexander.bryksin.kotlinspringcleanarchitecture.application.outbox.persistance.OutboxRepository
import com.alexander.bryksin.kotlinspringcleanarchitecture.application.common.publisher.EventPublisher
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.common.scope.eitherScope
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
    private val publisher: EventPublisher,
) {

    @Value("\${schedulers.outbox.batchSize}")
    private var batchSize: Int = 30

    @Scheduled(
        initialDelayString = "\${schedulers.outbox.initialDelayMillis}",
        fixedRateString = "\${schedulers.outbox.fixedRate}"
    )
    fun publishOutboxEvents() = runBlocking {
        eitherScope {
            outboxRepository.deleteEventsWithLock(batchSize) { publisher.publish(it) }.bind()
        }.fold(
            ifLeft = { err -> log.error { "error while publishing scheduler outbox events: $err" } },
            ifRight = { log.info { "outbox scheduler published events" } }
        )
    }

    private companion object {
        private val log = KotlinLogging.logger {}
    }
}