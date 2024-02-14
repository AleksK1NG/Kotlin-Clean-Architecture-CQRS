package com.alexander.bryksin.kotlinspringcleanarchitecture.infrastructure.account.persistance.repository

import com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.persistance.AccountProjectionRepository
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.models.Account
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.valueObjects.AccountId
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.*
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Component
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext


@Component
class AccountProjectionRepositoryImpl(private val databaseClient: DatabaseClient) : AccountProjectionRepository {

    override suspend fun createAccount(account: Account): Account = repositoryScope {
        // save account to mongo
        log.info { "AccountProjectionRepositoryImpl account: $account" }

        account
    }

    override suspend fun updateAccount(id: AccountId, account: Account): Account = repositoryScope {
        // find by id in mongo
        // update in mongo
        // if invalid version throw HigherAccountVersionException
        TODO("Not yet implemented")
    }

    override suspend fun getAccountById(id: AccountId): Account = repositoryScope {
        // get mongo account by id
        TODO("Not yet implemented")
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