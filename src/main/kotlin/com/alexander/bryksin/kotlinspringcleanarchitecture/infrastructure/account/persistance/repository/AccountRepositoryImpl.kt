package com.alexander.bryksin.kotlinspringcleanarchitecture.infrastructure.account.persistance.repository

import arrow.core.Either
import com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.persistance.AccountRepository
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.errors.AccountNotFoundError
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.errors.AppError
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.models.Account
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.valueObjects.AccountId
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.common.scope.eitherScope
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.r2dbc.core.awaitSingleOrNull
import org.springframework.r2dbc.core.bind
import org.springframework.stereotype.Repository
import java.time.Instant


@Repository
class AccountRepositoryImpl(
    private val dbClient: DatabaseClient
) : AccountRepository {

    override suspend fun saveAccount(account: Account): Either<AppError, Account> = eitherScope(repositoryContext) {
        dbClient.sql(INSERT_ACCOUNT_QUERY.trimMargin())
            .bindValues(account.withVersion(FIRST_VERSION).toPostgresEntityMap())
            .fetch()
            .rowsUpdated()
            .awaitSingle()
            .also { rowsUpdated -> log.info { "saved account rowsUpdated: $rowsUpdated, id: ${account.accountId}" } }

        account
    }


    override suspend fun updateAccount(account: Account): Either<AppError, Account> = eitherScope(repositoryContext) {
        val rowsUpdated = dbClient.sql(OPTIMISTIC_UPDATE_QUERY.trimMargin())
            .bindValues(account.withUpdatedAt(Instant.now()).toPostgresEntityMap(withOptimisticLock = true))
            .fetch()
            .rowsUpdated()
            .awaitSingle()

//        ensure(rowsUpdated == NO_ROWS_UPDATED) {
//            log.warn { "error no rows returns id: ${account.accountId} version: ${account.version}" }
//            GenericAppError("no rows updated: ${account.accountId} version: ${account.version}")
//        }

        account.incVersion().bind()
    }

    override suspend fun getAccountById(id: AccountId): Either<AppError, Account> = eitherScope(repositoryContext) {
        dbClient.sql(GET_ACCOUNT_BY_ID_QUERY.trimMargin())
            .bind(ID_FIELD, id.id)
            .map { row, _ -> row.toAccount() }
            .awaitSingleOrNull()
            .also { log.debug { "get account by id: $it" } }
            ?: raise(AccountNotFoundError("account for id: $id not found"))
    }


    private val repositoryContext = Job() + CoroutineName(this::class.java.name) + Dispatchers.IO
    private val scope = CoroutineScope(repositoryContext)

    private companion object {
        private val log = KotlinLogging.logger { }
        private const val FIRST_VERSION = 1L
        private const val NO_ROWS_UPDATED = 0L
        private const val ID_FIELD = "id"
    }
}

