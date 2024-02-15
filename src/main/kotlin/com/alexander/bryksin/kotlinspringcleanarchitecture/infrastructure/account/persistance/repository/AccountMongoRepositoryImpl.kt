package com.alexander.bryksin.kotlinspringcleanarchitecture.infrastructure.account.persistance.repository

import com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.persistance.AccountMongoRepository
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
class AccountMongoRepositoryImpl(
    private val mongoClient: MongoClient,
) : AccountMongoRepository {

    private val accountsDB = mongoClient.getDatabase("accounts")
    private val accountsCollection = accountsDB.getCollection<AccountDocument>("accounts")

    override suspend fun createAccount(account: Account): Account = repositoryScope {
        val insertOneResult = try {
            accountsCollection.insertOne(account.toDocument())
        } catch (e: Exception) {
            log.error { "error inserting account ${e.message}" }
            throw e
        }
        // save account to mongo
        log.info { "AccountProjectionRepositoryImpl account insertOneResult: $insertOneResult, account: $account" }

        account
    }

    override suspend fun updateAccount(account: Account): Account = repositoryScope {
        val accountDocument = accountsCollection.find(eq("accountId", account.accountId?.string())).firstOrNull()
            ?: throw AccountNotFoundException(account.accountId?.string())

        val updatedAccount = accountDocument.toAccount().copy(
            contactInfo = account.contactInfo,
            personalInfo = account.personalInfo,
            address = account.address,
            balance = account.balance,
            status = account.status,
            version = accountDocument.version + 1,
            updatedAt = Instant.now(),
        )


        val filter = and(eq("accountId", account.accountId?.string()), eq("version", account.version))
        val options = FindOneAndUpdateOptions().upsert(false).returnDocument(ReturnDocument.AFTER)

        val updatedDocument = accountsCollection.findOneAndUpdate(
            filter,
            updatedAccount.toBsonUpdate(),
            options
        ) ?: throw AccountNotFoundException(account.accountId?.string())

        updatedDocument.toAccount()
    }

    override suspend fun getAccountById(id: AccountId): Account? = repositoryScope {
        try {
            val document = accountsCollection.find<AccountDocument>(eq("accountId", id.string())).firstOrNull()
            log.info { "AccountProjectionRepositoryImpl getAccountByIdDocument: $document" }
            document?.toAccount()
        } catch (e: Exception) {
            log.error { "error getAccountById ${e.message}" }
            throw e
        }
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