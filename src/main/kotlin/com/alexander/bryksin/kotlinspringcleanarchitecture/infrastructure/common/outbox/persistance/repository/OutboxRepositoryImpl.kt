package com.alexander.bryksin.kotlinspringcleanarchitecture.infrastructure.common.outbox.persistance.repository

import com.alexander.bryksin.kotlinspringcleanarchitecture.application.common.outbox.persistance.OutboxRepository
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.common.outbox.models.OutboxEvent
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Component
import org.springframework.transaction.reactive.TransactionalOperator
import org.springframework.transaction.reactive.executeAndAwait
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

@Component
class OutboxRepositoryImpl(
    private val dbClient: DatabaseClient,
    private val tx: TransactionalOperator
) : OutboxRepository {

    override suspend fun insert(event: OutboxEvent): OutboxEvent = withContext(Dispatchers.IO) {
        try {
            val savedEventId = dbClient.sql(INSERT_OUTBOX_EVENT_QUERY.trimMargin())
                .bindValues(event.toPostgresValuesMap())
                .map { row, _ -> row.get("event_id", String::class.java) }
                .one()
                .awaitSingleOrNull()

            log.info { "saved event: $savedEventId" }

            event
        } catch (e: Exception) {
            log.error { "error inserting event: ${e.message}" }
            throw e
        }
    }

    override suspend fun deleteWithLock(
        event: OutboxEvent,
        callback: suspend (event: OutboxEvent) -> Unit
    ): OutboxEvent = withContext(Dispatchers.IO) {
        tx.executeAndAwait {
            val lockedEventId = dbClient.sql(GET_OUTBOX_EVENT_BY_ID_FOR_UPDATE_SKIP_LOCKED_QUERY.trimMargin())
                .bind("eventId", event.eventId!!)
                .map { row, _ -> row.get("event_id", String::class.java) }
                .one()
                .awaitSingleOrNull()

            log.info { "selected for update event id: ${event.eventId}, skip locked: $lockedEventId" }
            if (lockedEventId == null) return@executeAndAwait

            callback(event)

            val rowsDeleted = dbClient.sql(DELETE_OUTBOX_EVENT_BY_ID_QUERY)
                .bind("eventId", event.eventId)
                .fetch()
                .rowsUpdated()
                .awaitSingle()

            log.info { "eventId:${event.eventId} rowsDeleted: $rowsDeleted" }
        }

        event
    }

    override suspend fun deleteEventsWithLock(
        batchSize: Int,
        callback: suspend (event: OutboxEvent) -> Unit
    ): Unit = repositoryScope {
        log.debug { "starting to publish events with lock count: $batchSize" }

        tx.executeAndAwait {
            dbClient.sql(GET_OUTBOX_EVENTS_FOR_UPDATE_SKIP_LOCKED_QUERY.trimMargin())
                .bind("limit", batchSize)
                .map { row, _ -> row.toOutboxEvent() }
                .all()
                .asFlow()
                .onStart { log.info { "start publishing outbox events: $batchSize" } }
                .onEach { callback(it) }
                .onEach { event ->
                    dbClient.sql(DELETE_OUTBOX_EVENT_BY_ID_QUERY)
                        .bind("eventId", event.eventId!!)
                        .fetch()
                        .rowsUpdated()
                        .awaitSingle()
                        .also { rowsDeleted -> log.info { "eventId: ${event.eventId} rowsDeleted: $rowsDeleted" } }
                }
                .onCompletion { log.info { "completed publishing outbox events: $batchSize" } }
                .collect()
        }

        log.debug { "finished publishing events with lock count: $batchSize" }
    }


    private val scope = CoroutineScope(Job() + CoroutineName(this::class.java.name) + Dispatchers.IO)

    private suspend fun <T> repositoryScope(
        context: CoroutineContext = EmptyCoroutineContext,
        block: suspend (CoroutineScope) -> T
    ): T = block(scope + context)

    private companion object {
        private val log = KotlinLogging.logger { }
    }
}

