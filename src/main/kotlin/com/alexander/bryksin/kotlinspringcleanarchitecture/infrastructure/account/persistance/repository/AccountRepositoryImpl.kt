package com.alexander.bryksin.kotlinspringcleanarchitecture.infrastructure.account.persistance.repository

import com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.persistance.AccountRepository
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.models.Account
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.valueObjects.AccountId
import com.alexander.bryksin.kotlinspringcleanarchitecture.infrastructure.account.exceptions.NoRowsUpdatedException
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.*
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.r2dbc.core.awaitSingleOrNull
import org.springframework.r2dbc.core.bind
import org.springframework.stereotype.Repository
import java.time.Instant
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext


@Repository
class AccountRepositoryImpl(
    private val dbClient: DatabaseClient
) : AccountRepository {

    override suspend fun saveAccount(account: Account): Account = repositoryScope {
        dbClient.sql(INSERT_ACCOUNT_QUERY.trimMargin())
            .bindValues(account.withVersion(FIRST_VERSION).toPostgresEntityMap())
            .fetch()
            .rowsUpdated()
            .awaitSingle()
            .also { rowsUpdated -> log.info { "saved account rowsUpdated: $rowsUpdated, id: ${account.accountId}" } }

        account
    }


    override suspend fun updateAccount(account: Account): Account = repositoryScope {
        val rowsUpdated = dbClient.sql(OPTIMISTIC_UPDATE_QUERY.trimMargin())
            .bindValues(account.withUpdatedAt(Instant.now()).toPostgresEntityMap(withOptimisticLock = true))
            .fetch()
            .rowsUpdated()
            .awaitSingle()

        if (rowsUpdated == NO_ROWS_UPDATED) {
            log.warn { "error optimistic lock while updating id: ${account.accountId} version: ${account.version}" }
            throw NoRowsUpdatedException(account.accountId, account.version)
        }

        account.incVersion()
    }

    override suspend fun getAccountById(id: AccountId): Account? = repositoryScope {
        dbClient.sql(GET_ACCOUNT_BY_ID_QUERY.trimMargin())
            .bind("id", id.id)
            .map { row, _ -> row.toAccount() }
            .awaitSingleOrNull()
            .also { log.debug { "get account by id: $it" } }
    }

    private val scope = CoroutineScope(Job() + CoroutineName(this::class.java.name) + Dispatchers.IO)

    private suspend fun <T> repositoryScope(
        context: CoroutineContext = EmptyCoroutineContext,
        block: suspend (CoroutineScope) -> T
    ): T = block(scope + context)

    private companion object {
        private val log = KotlinLogging.logger { }
        private const val FIRST_VERSION = 1L
        private const val NO_ROWS_UPDATED = 0L
    }
}

