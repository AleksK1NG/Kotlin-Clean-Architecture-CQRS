package com.alexander.bryksin.kotlinspringcleanarchitecture.infrastructure.account.persistance.repository

import com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.persistance.AccountRepository
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.models.Account
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.valueObjects.AccountId
import com.alexander.bryksin.kotlinspringcleanarchitecture.infrastructure.account.exceptions.AccountOptimisticUpdateException
import com.alexander.bryksin.kotlinspringcleanarchitecture.infrastructure.account.persistance.entity.toAccount
import com.alexander.bryksin.kotlinspringcleanarchitecture.infrastructure.account.persistance.entity.toAccountEntity
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.withContext
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.r2dbc.core.awaitSingleOrNull
import org.springframework.stereotype.Repository
import java.time.Instant


@Repository
class AccountRepositoryImpl(
    private val db: AccountsCoroutineStore,
    private val dbClient: DatabaseClient
) : AccountRepository {


    override suspend fun saveAccount(account: Account): Account = withContext(Dispatchers.IO) {
        try {
            val insertResult = dbClient.sql(INSERT_ACCOUNT_QUERY.trimMargin())
                .bindValues(account.copy(version = 1).toPostgresEntityMap())
                .fetch()
                .rowsUpdated()
                .awaitSingle()

            log.info { "Saved account into database: $insertResult" }
        } catch (e: Exception) {
            log.error { "error while saving account into database: ${e.message}" }
            throw e
        }

        account
    }


    override suspend fun createAccount(account: Account): Account = withContext(Dispatchers.IO) {
        try {
            val entity = account.toAccountEntity()
            val savedAccount = db.save(entity)
            log.info { "saved account: $savedAccount" }
            savedAccount.toAccount()
        } catch (e: Exception) {
            log.error { "error creating new account: ${e.message}" }
            throw e
        }
    }

    override suspend fun updateAccount(account: Account): Account = withContext(Dispatchers.IO) {
        try {
            val rowsUpdated = dbClient.sql(OPTIMISTIC_UPDATE_QUERY.trimMargin())
                .bindValues(account.copy(updatedAt = Instant.now()).toPostgresEntityMap(withOptimisticLock = true))
                .fetch()
                .rowsUpdated()
                .awaitSingle()

            if (rowsUpdated == 0L) {
                log.error { "optimistic updated error account: $account" }
                throw AccountOptimisticUpdateException(account)
            }

            account.copy(version = account.version + 1)
        } catch (e: Exception) {
            log.error { "error updating new account: ${e.message}" }
            throw e
        }
    }

    override suspend fun getAccountById(id: AccountId): Account? = withContext(Dispatchers.IO) {
        val account = dbClient.sql(
            """SELECT id, email, phone, country, city, post_code,
            | bio, image_url, balance_amount, balance_currency, status, 
            | version, created_at, updated_at 
            | FROM microservices.accounts a 
            | WHERE id = :id""".trimMargin()
        )
            .bind("id", id.id)
            .map { row, _ -> row.toAccount() }
            .awaitSingleOrNull()

        log.info { "get account from database: $account" }
        account
    }


    private companion object {
        private val log = KotlinLogging.logger { }
    }
}

