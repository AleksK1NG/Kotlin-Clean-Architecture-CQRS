package com.alexander.bryksin.kotlinspringcleanarchitecture.infrastructure.account.persistance.repository

import com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.persistance.AccountRepository
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.models.Account
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.valueObjects.AccountId
import com.alexander.bryksin.kotlinspringcleanarchitecture.infrastructure.account.persistance.entity.toAccount
import com.alexander.bryksin.kotlinspringcleanarchitecture.infrastructure.account.persistance.entity.toAccountEntity
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.withContext
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Repository
import java.time.Instant


@Repository
class AccountRepositoryImpl(
    private val db: AccountsCoroutineStore,
    private val dbClient: DatabaseClient
) : AccountRepository {


    override suspend fun saveAccount(account: Account): Account = withContext(Dispatchers.IO) {
        try {
            val insertResult = dbClient.sql(
                """INSERT INTO microservices.accounts
                | (id, email, phone, country, city, post_code, bio, image_url, balance_amount, balance_currency, status, version, created_at, updated_at) 
                | VALUES (:id, :email, :phone, :country, :city, :post_code, :bio, :image_url, :balance_amount, :balance_currency, :status, :version, :created_at, :updated_at)""".trimMargin()
            )
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
            dbClient.sql(
                """UPDATE microservices.accounts a 
                |SET email = :email, phone = :phone, country = :country, city = :city, post_code= :post_code, bio = :bio,
                |image_url = :image_url, balance_amount = :balance_amount, balance_currency = :balance_currency, status = :status,
                |version = :version, updated_at = :updated_at, created_at = :created_at
                |WHERE a.id = :id and version = :prev_version""".trimMargin()
            )
                .bindValues(account.copy(updatedAt = Instant.now()).toPostgresEntityMap(withOptimisticLock = true))
                .fetch()
                .rowsUpdated()
                .awaitSingle()

            account.copy(version = account.version + 1)
        } catch (e: Exception) {
            log.error { "error updating new account: ${e.message}" }
            throw e
        }
    }

    override suspend fun getAccountById(id: AccountId): Account? = withContext(Dispatchers.IO) {
        val account = db.findById(id.id)
        account?.toAccount().also { log.info { "found account: $account" } }
    }

    private companion object {
        private val log = KotlinLogging.logger { }
    }
}

fun Account.toPostgresEntityMap(withOptimisticLock: Boolean = false): MutableMap<String, *> {
    val map = mutableMapOf(
        "id" to accountId?.id,
        "email" to contactInfo.email,
        "phone" to contactInfo.phone,
        "country" to address.country,
        "city" to address.city,
        "post_code" to address.postCode,
        "bio" to personalInfo.bio,
        "image_url" to personalInfo.imageUrl,
        "balance_amount" to balance.amount,
        "balance_currency" to balance.balanceCurrency.name,
        "status" to status.name,
        "version" to version,
        "created_at" to createdAt,
        "updated_at" to updatedAt,
    )

    if (withOptimisticLock) {
        map["version"] = version + 1
        map["prev_version"] = version
    }

    return map
}