package com.alexander.bryksin.kotlinspringcleanarchitecture.infrastructure.account.persistance.repository

import arrow.core.Either
import arrow.fx.coroutines.parZip
import com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.models.AccountsList
import com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.persistance.AccountProjectionRepository
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.errors.AccountNotFoundError
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.errors.AppError
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.models.Account
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.valueObjects.AccountId
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.common.scope.eitherScope
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
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.count
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import org.springframework.stereotype.Component


@Component
class AccountProjectionRepositoryImpl(
    mongoClient: MongoClient,
) : AccountProjectionRepository {

    private val accountsDB = mongoClient.getDatabase(ACCOUNTS_DB)
    private val accountsCollection = accountsDB.getCollection<AccountDocument>(ACCOUNTS_COLLECTION)

    override suspend fun createAccount(account: Account): Either<AppError, Account> = eitherScope(ctx) {
        val insertOneResult = accountsCollection.insertOne(account.toDocument())
        log.info { "account insertOneResult: ${insertOneResult}, account: $account" }
        account
    }

    override suspend fun updateAccount(account: Account): Either<AppError, Account> = eitherScope(ctx) {
        val filter = and(eq(ACCOUNT_ID, account.accountId.string()), eq(VERSION, account.version))
        val options = FindOneAndUpdateOptions().upsert(false).returnDocument(ReturnDocument.AFTER)

        accountsCollection.findOneAndUpdate(
            filter,
            account.incVersion().bind().toBsonUpdate(),
            options
        )
            ?.toAccount()
            ?: raise(AccountNotFoundError("account with id: ${account.accountId} not found"))
    }

    override suspend fun upsert(account: Account): Either<AppError, Account> = eitherScope(ctx) {
        val filter = and(eq(ACCOUNT_ID, account.accountId.string()))
        val options = FindOneAndUpdateOptions().upsert(true).returnDocument(ReturnDocument.AFTER)

        accountsCollection.findOneAndUpdate(
            filter,
            account.incVersion().bind().toBsonUpdate(),
            options
        )
            ?.toAccount()
            ?: raise(AccountNotFoundError("account with id: ${account.accountId} not found"))
    }

    override suspend fun getAccountById(id: AccountId): Either<AppError, Account> = eitherScope(ctx) {
        accountsCollection.find<AccountDocument>(eq(ACCOUNT_ID, id.string()))
            .firstOrNull()
            ?.toAccount()
            ?: raise(AccountNotFoundError("account with id: $id not found"))
    }

    override suspend fun getAccountByEmail(email: String): Either<AppError, Account> = eitherScope(ctx) {
        val filter = and(eq(CONTACT_INFO_EMAIL, email))
        accountsCollection.find(filter).firstOrNull()?.toAccount()
            ?: raise(AccountNotFoundError("account with email: $email not found"))
    }

    override suspend fun getAllAccounts(page: Int, size: Int): Either<AppError, AccountsList> = eitherScope(ctx) {
        parZip(coroutineContext, {
            accountsCollection.find()
                .skip(page * size)
                .limit(size)
                .map { it.toAccount() }
                .toList()
        }, {
            accountsCollection.find().count()
        }) { list, totalCount ->
            AccountsList(
                page = page,
                size = size,
                totalCount = totalCount,
                accountsList = list
            )
        }
    }

    private val ctx = Job() + CoroutineName(this::class.java.name) + Dispatchers.IO

    private companion object {
        private val log = KotlinLogging.logger { }

        private const val ACCOUNTS_DB = "accounts"
        private const val ACCOUNTS_COLLECTION = "accounts"
        private const val ACCOUNT_ID = "accountId"
        private const val VERSION = "version"
        private const val CONTACT_INFO_EMAIL = "contactInfo.email"
    }
}