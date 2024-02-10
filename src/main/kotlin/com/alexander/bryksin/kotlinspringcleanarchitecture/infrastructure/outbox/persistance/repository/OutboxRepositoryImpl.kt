package com.alexander.bryksin.kotlinspringcleanarchitecture.infrastructure.outbox.persistance.repository

import com.alexander.bryksin.kotlinspringcleanarchitecture.application.outbox.persistance.OutboxRepository
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.outbox.models.OutboxEvent
import kotlinx.coroutines.Dispatchers
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

    override suspend fun insert(event: OutboxEvent) {
        TODO("Not yet implemented")
    }

    suspend fun deleteWithLock(event: OutboxEvent, cb: suspend (event: OutboxEvent) -> Unit) =
        withContext(Dispatchers.IO) {
            tx.executeAndAwait {
                dbClient.sql("""SELECT * FROM microservices.accounts a WHERE a.id = :id FOR UPDATE SKIP LOCKED """.trimIndent())
                    .bind("id", event.eventId)
                    .map { row, meta ->
                        row.get("id", String::class.java)
                    }
                    .one()
                    .awaitSingleOrNull()


            }
        }
}