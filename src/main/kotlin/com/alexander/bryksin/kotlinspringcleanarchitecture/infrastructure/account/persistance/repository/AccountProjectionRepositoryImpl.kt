package com.alexander.bryksin.kotlinspringcleanarchitecture.infrastructure.account.persistance.repository

import com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.persistance.AccountProjectionRepository
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.models.Account
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.valueObjects.AccountId
import com.alexander.bryksin.kotlinspringcleanarchitecture.infrastructure.account.persistance.entity.AccountDocument
import com.alexander.bryksin.kotlinspringcleanarchitecture.infrastructure.account.persistance.entity.toAccount
import com.alexander.bryksin.kotlinspringcleanarchitecture.infrastructure.account.persistance.entity.toBsonUpdate
import com.alexander.bryksin.kotlinspringcleanarchitecture.infrastructure.account.persistance.entity.toDocument
import com.mongodb.client.model.Filters.and
import com.mongodb.client.model.Filters.eq
import com.mongodb.client.model.FindOneAndUpdateOptions
import com.mongodb.client.model.ReturnDocument
import com.mongodb.kotlin.client.coroutine.MongoClient
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.firstOrNull
import org.springframework.stereotype.Component
import java.time.Instant
import javax.security.auth.login.AccountNotFoundException
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext


@Component
class AccountProjectionRepositoryImpl(
    mongoClient: MongoClient,
) : AccountProjectionRepository {

    private val accountsDB = mongoClient.getDatabase(ACCOUNTS_DB)
    private val accountsCollection = accountsDB.getCollection<AccountDocument>(ACCOUNTS_COLLECTION)

    override suspend fun createAccount(account: Account): Account = repositoryScope {
        val insertOneResult = accountsCollection.insertOne(account.toDocument())
        log.info { "account insertOneResult: $insertOneResult, account: $account" }
        account
    }

    override suspend fun updateAccount(account: Account): Account = repositoryScope {
//        val accountDocument = accountsCollection.find(eq(ACCOUNT_ID, account.accountId?.string()))
//            .firstOrNull()
//            ?: throw AccountNotFoundException(account.accountId?.string())

//        val updatedAccount = accountDocument.toAccount().copy(
//            contactInfo = account.contactInfo,
//            personalInfo = account.personalInfo,
//            address = account.address,
//            balance = account.balance,
//            status = account.status,
//            version = accountDocument.version + 1,
//            updatedAt = Instant.now(),
//        )

        try {
            val filter = and(eq(ACCOUNT_ID, account.accountId?.string()), eq(VERSION, account.version))
            val options = FindOneAndUpdateOptions().upsert(false).returnDocument(ReturnDocument.AFTER)

            val res = accountsCollection.findOneAndUpdate(
                filter,
                account.copy(version = account.version + 1, updatedAt = Instant.now()).toBsonUpdate(),
                options
            )
                ?.toAccount()
                ?: throw AccountNotFoundException(account.accountId?.string())

            res
        } catch (e: Exception) {
            log.error { e.message }
            throw e
        }
    }

    override suspend fun getAccountById(id: AccountId): Account? = repositoryScope {
        accountsCollection.find<AccountDocument>(eq(ACCOUNT_ID, id.string()))
            .firstOrNull()
            ?.toAccount()
    }


    private val scope = CoroutineScope(Job() + CoroutineName(this::class.java.name) + Dispatchers.IO)

    private suspend fun <T> repositoryScope(
        context: CoroutineContext = EmptyCoroutineContext,
        block: suspend (CoroutineScope) -> T
    ): T = block(scope + context)

    private companion object {
        private val log = KotlinLogging.logger { }

        private const val ACCOUNTS_DB = "accounts"
        private const val ACCOUNTS_COLLECTION = "accounts"
        private const val ACCOUNT_ID = "accountId"
        private const val VERSION = "version"
    }
}