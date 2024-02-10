package com.alexander.bryksin.kotlinspringcleanarchitecture.infrastructure.outbox.persistance.repository

import com.alexander.bryksin.kotlinspringcleanarchitecture.application.outbox.persistance.OutboxRepository
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.outbox.models.OutboxEvent
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import kotlinx.coroutines.withContext
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Component
import org.springframework.transaction.reactive.TransactionalOperator
import org.springframework.transaction.reactive.executeAndAwait


@Component
class OutboxRepositoryImpl(
    private val dbClient: DatabaseClient,
    private val tx: TransactionalOperator
) : OutboxRepository {

    override suspend fun insert(event: OutboxEvent): OutboxEvent = withContext(Dispatchers.IO) {
        try {
            val savedEventId = dbClient.sql(
                """INSERT INTO microservices.outbox_table
                    | (event_id, aggregate_id, event_type, version, data, timestamp) 
                    | VALUES (:event_id, :aggregate_id, :event_type, :version, :data, :timestamp)
                    | RETURNING event_id""".trimMargin()
            )
                .bind("event_id", event.eventId!!)
                .bind("aggregate_id", event.aggregateId)
                .bind("event_type", event.eventType)
                .bind("version", event.version)
                .bind("data", event.data)
                .bind("timestamp", event.timestamp)
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
            val lockedEventId = dbClient.sql(
                """SELECT event_id 
                |FROM microservices.outbox_table ot 
                |WHERE ot.event_id = :eventId 
                |FOR UPDATE SKIP LOCKED """.trimMargin()
            )
                .bind("eventId", event.eventId!!)
                .map { row, _ -> row.get("event_id", String::class.java) }
                .one()
                .awaitSingleOrNull()

            log.info { "selected for update event id: ${event.eventId}, skip locked: $lockedEventId" }
            if (lockedEventId == null) return@executeAndAwait

            callback(event)

            val rowsDeleted = dbClient.sql("DELETE FROM microservices.outbox_table WHERE event_id = :eventId ")
                .bind("eventId", event.eventId)
                .fetch()
                .rowsUpdated()
                .awaitSingle()
            log.info { "rowsDeleted: $rowsDeleted" }
        }

        event
    }


    private companion object {
        private val log = KotlinLogging.logger { }
    }
}