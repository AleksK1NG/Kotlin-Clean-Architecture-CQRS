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

    override suspend fun insert(event: OutboxEvent): OutboxEvent = repositoryScope {
        dbClient.sql(INSERT_OUTBOX_EVENT_QUERY.trimMargin())
            .bindValues(event.toPostgresValuesMap())
            .map { row, _ -> row.get(ROW_EVENT_ID, String::class.java) }
            .one()
            .awaitSingle()
            .also { log.info { "saved event: $it" } }

        event
    }

    override suspend fun deleteWithLock(
        event: OutboxEvent,
        callback: suspend (event: OutboxEvent) -> Unit
    ): OutboxEvent = repositoryScope {
        tx.executeAndAwait {
            dbClient.sql(GET_OUTBOX_EVENT_BY_ID_FOR_UPDATE_SKIP_LOCKED_QUERY.trimMargin())
                .bindValues(mutableMapOf("eventId" to event.eventId))
                .map { row, _ -> row.get(ROW_EVENT_ID, String::class.java) }
                .one()
                .awaitSingleOrNull()
                .let { callback(event) }
                .let { deleteOutboxEvent(event) }
                .let { _ -> event }
        }
    }


    override suspend fun deleteEventsWithLock(
        batchSize: Int,
        callback: suspend (event: OutboxEvent) -> Unit
    ): Unit = repositoryScope {
        tx.executeAndAwait {
            dbClient.sql(GET_OUTBOX_EVENTS_FOR_UPDATE_SKIP_LOCKED_QUERY.trimMargin())
                .bind("limit", batchSize)
                .map { row, _ -> row.toOutboxEvent() }
                .all()
                .asFlow()
                .onStart { log.info { "start publishing outbox events batch: $batchSize" } }
                .onEach { callback(it) }
                .onEach { event -> deleteOutboxEvent(event) }
                .onCompletion { log.info { "completed publishing outbox events batch: $batchSize" } }
                .collect()
        }
    }

    private suspend fun deleteOutboxEvent(event: OutboxEvent) {
        dbClient.sql(DELETE_OUTBOX_EVENT_BY_ID_QUERY)
            .bindValues(mutableMapOf("eventId" to event.eventId))
            .fetch()
            .rowsUpdated()
            .awaitSingle()
            .also { rowsDeleted -> log.info { "eventId: ${event.eventId} rowsDeleted: $rowsDeleted" } }
    }


    private val scope = CoroutineScope(Job() + CoroutineName(this::class.java.name) + Dispatchers.IO)

    private suspend fun <T> repositoryScope(
        context: CoroutineContext = EmptyCoroutineContext,
        block: suspend CoroutineScope.() -> T
    ): T = block(scope + context)

    private companion object {
        private val log = KotlinLogging.logger { }
        private const val ROW_EVENT_ID = "event_id"
    }
}

