package com.alexander.bryksin.kotlinspringcleanarchitecture.infrastructure.common.outbox.persistance.repository

import com.alexander.bryksin.kotlinspringcleanarchitecture.application.common.outbox.persistance.OutboxRepository
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.common.outbox.models.OutboxEvent
import io.github.oshai.kotlinlogging.KotlinLogging
import io.r2dbc.spi.Row
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import kotlinx.coroutines.withContext
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Component
import org.springframework.transaction.reactive.TransactionalOperator
import org.springframework.transaction.reactive.executeAndAwait
import java.math.BigInteger
import java.time.Instant
import java.util.*


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

            log.info { "eventId:${event.eventId} rowsDeleted: $rowsDeleted" }
        }

        event
    }

    override suspend fun deleteEventsWithLock(
        batchSize: Int,
        callback: suspend (event: OutboxEvent) -> Unit
    ): Unit = withContext(Dispatchers.IO) {
        log.debug { "starting to publish events with lock count: $batchSize" }
        tx.executeAndAwait {
            dbClient.sql(
                """SELECT ot.event_id, ot.event_type, ot.aggregate_id, ot.data, ot.version, ot.timestamp
                |FROM microservices.outbox_table ot 
                |ORDER BY ot.timestamp ASC
                |LIMIT :limit
                |FOR UPDATE SKIP LOCKED """.trimMargin()
            )
                .bind("limit", batchSize)
                .map { row, _ -> row.toOutboxEvent() }
                .all()
                .asFlow()
                .onStart { log.info { "start publishing outbox events: $batchSize" } }
                .onEach { outboxEvent ->

                    callback(outboxEvent)

                    val rowsDeleted = dbClient.sql("DELETE FROM microservices.outbox_table WHERE event_id = :eventId ")
                        .bind("eventId", outboxEvent.eventId!!)
                        .fetch()
                        .rowsUpdated()
                        .awaitSingle()

                    log.info { "eventId: ${outboxEvent.eventId} rowsDeleted: $rowsDeleted" }
                }
                .onCompletion { log.info { "completed publishing outbox events: $batchSize" } }
                .collect()
        }
        log.debug { "finished publishing events with lock count: $batchSize" }
    }


    private companion object {
        private val log = KotlinLogging.logger { }
    }
}


fun Row.toOutboxEvent(): OutboxEvent = OutboxEvent(
    eventId = UUID.fromString(get("event_id", String::class.java)),
    eventType = get("event_type", String::class.java) ?: "",
    aggregateId = get("aggregate_id", String::class.java) ?: "",
    data = get("data", ByteArray::class.java) ?: byteArrayOf(),
    version = get("version", BigInteger::class.java)?.toLong() ?: 0,
    timestamp = get("timestamp", Instant::class.java) ?: Instant.now(),
)
