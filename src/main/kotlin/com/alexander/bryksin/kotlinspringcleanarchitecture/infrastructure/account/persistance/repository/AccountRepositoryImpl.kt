package com.alexander.bryksin.kotlinspringcleanarchitecture.infrastructure.account.persistance.repository

import com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.persistance.AccountRepository
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.models.Account
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.valueObjects.AccountId
import com.alexander.bryksin.kotlinspringcleanarchitecture.infrastructure.account.exceptions.AccountOptimisticUpdateException
import com.alexander.bryksin.kotlinspringcleanarchitecture.infrastructure.account.persistance.entity.toAccount
import com.alexander.bryksin.kotlinspringcleanarchitecture.infrastructure.account.persistance.entity.toAccountEntity
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.*
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.r2dbc.core.awaitSingleOrNull
import org.springframework.stereotype.Repository
import java.time.Instant
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext


@Repository
class AccountRepositoryImpl(
    private val db: AccountsCoroutineStore,
    private val dbClient: DatabaseClient
) : AccountRepository {

    override suspend fun saveAccount(account: Account): Account = withContext(Dispatchers.IO) {
        val rowsUpdated = dbClient.sql(INSERT_ACCOUNT_QUERY.trimMargin())
            .bindValues(account.copy(version = 1).toPostgresEntityMap())
            .fetch()
            .rowsUpdated()
            .awaitSingle()
        log.info { "saved account: $rowsUpdated, id: ${account.accountId?.id}" }
        account
    }


    override suspend fun createAccount(account: Account): Account = withContext(Dispatchers.IO) {
        val entity = account.toAccountEntity()
        val savedAccount = db.save(entity)
        log.info { "saved account: $savedAccount" }
        savedAccount.toAccount()
    }


    override suspend fun updateAccount(account: Account): Account = repositoryScope(Dispatchers.IO) {
        try {
            val rowsUpdated = dbClient.sql(OPTIMISTIC_UPDATE_QUERY.trimMargin())
                .bindValues(account.copy(updatedAt = Instant.now()).toPostgresEntityMap(withOptimisticLock = true))
                .fetch()
                .rowsUpdated()
                .awaitSingle()

            if (rowsUpdated == 0L) {
                log.error { "optimistic lock error while updating account: ${account.accountId?.id}" }
                throw AccountOptimisticUpdateException(account)
            }

            account.copy(version = account.version + 1)
        } catch (e: Exception) {
            log.error { "error updating new account: ${e.message}" }
            throw e
        }
    }

    override suspend fun getAccountById(id: AccountId): Account? = repositoryScope {
        dbClient.sql(GET_ACCOUNT_BY_ID_QUERY.trimMargin())
            .bind("id", id.id)
            .map { row, _ -> row.toAccount() }
            .awaitSingleOrNull()
            .also { log.info { "get account by id result: $it" } }
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

