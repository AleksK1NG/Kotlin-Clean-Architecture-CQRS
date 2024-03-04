package com.alexander.bryksin.kotlinspringcleanarchitecture.infrastructure.outbox.repository

import arrow.core.Either
import com.alexander.bryksin.kotlinspringcleanarchitecture.application.outbox.persistance.OutboxRepository
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.errors.AppError
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.common.scope.eitherScope
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.outbox.models.OutboxEvent
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
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

@Component
class OutboxRepositoryImpl(
    private val dbClient: DatabaseClient,
    private val tx: TransactionalOperator
) : OutboxRepository {

    override suspend fun insert(event: OutboxEvent): Either<AppError, OutboxEvent> = eitherScope(ctx) {
        dbClient.sql(INSERT_OUTBOX_EVENT_QUERY.trimMargin())
            .bindValues(event.toPostgresValuesMap())
            .map { row, _ -> row.get(ROW_EVENT_ID, String::class.java) }
            .one()
            .awaitSingle()
            .let { event }
    }


    override suspend fun deleteWithLock(
        event: OutboxEvent,
        callback: suspend (event: OutboxEvent) -> Either<AppError, Unit>
    ): Either<AppError, OutboxEvent> = eitherScope {
        tx.executeAndAwait {
            dbClient.sql(GET_OUTBOX_EVENT_BY_ID_FOR_UPDATE_SKIP_LOCKED_QUERY.trimMargin())
                .bindValues(mutableMapOf(EVENT_ID to event.eventId))
                .map { row, _ -> row.get(ROW_EVENT_ID, String::class.java) }
                .one()
                .awaitSingleOrNull()

            callback(event).bind()
            deleteOutboxEvent(event).bind()
            event
        }
    }


    override suspend fun deleteEventsWithLock(
        batchSize: Int,
        callback: suspend (event: OutboxEvent) -> Either<AppError, Unit>
    ): Either<AppError, Unit> = eitherScope(ctx) {
        tx.executeAndAwait {
            dbClient.sql(GET_OUTBOX_EVENTS_FOR_UPDATE_SKIP_LOCKED_QUERY.trimMargin())
                .bind(LIMIT, batchSize)
                .map { row, _ -> row.toOutboxEvent() }
                .all()
                .asFlow()
                .onStart { log.info { "start publishing outbox events batch: $batchSize" } }
                .onEach { callback(it).bind() }
                .onEach { event -> deleteOutboxEvent(event).bind() }
                .onCompletion { log.info { "completed publishing outbox events batch: $batchSize" } }
                .collect()
        }
    }

    private suspend fun deleteOutboxEvent(event: OutboxEvent): Either<AppError, Long> = eitherScope(ctx) {
        dbClient.sql(DELETE_OUTBOX_EVENT_BY_ID_QUERY)
            .bindValues(mutableMapOf(EVENT_ID to event.eventId))
            .fetch()
            .rowsUpdated()
            .awaitSingle()
            .also { rowsDeleted -> log.info { "eventId: ${event.eventId} rowsDeleted: $rowsDeleted" } }
    }

    private val ctx = Job() + CoroutineName(this::class.java.name) + Dispatchers.IO

    private companion object {
        private val log = KotlinLogging.logger { }
        private const val ROW_EVENT_ID = "event_id"
        private const val LIMIT = "limit"
        private const val EVENT_ID = "eventId"
    }
}

