package com.alexander.bryksin.kotlinspringcleanarchitecture.infrastructure.account.persistance.repository

import com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.persistance.AccountRepository
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.models.Account
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.valueObjects.AccountId
import com.alexander.bryksin.kotlinspringcleanarchitecture.infrastructure.account.persistance.entity.toAccount
import com.alexander.bryksin.kotlinspringcleanarchitecture.infrastructure.account.persistance.entity.toAccountEntity
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.reactor.awaitSingleOrNull
import kotlinx.coroutines.withContext
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Repository


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
//                .bind("id", account.accountId?.id!!)
//                .bind("email", account.contactInfo.email)
//                .bind("phone", account.contactInfo.phone)
//                .bind("country", account.address.country ?: "")
//                .bind("city", account.address.city ?: "")
//                .bind("post_code", account.address.postCode ?: "")
//                .bind("bio", account.personalInfo.bio)
//                .bind("image_url", account.personalInfo.imageUrl)
//                .bind("balance_amount", account.balance.amount)
//                .bind("balance_currency", account.balance.balanceCurrency.name)
//                .bind("status", account.status.name)
//                .bind("version", 1)
//                .bind("created_at", account.createdAt ?: Instant.now())
//                .bind("updated_at", account.updatedAt ?: Instant.now())
                //            .map { row, _ -> row }
                .fetch()
                .one()
                .awaitSingleOrNull()


            log.info { "Saved account into database: $insertResult" }
        } catch (e: Exception) {
            log.error { "error while saving account into database: ${e.message}" }
            throw e
        }

        account
    }


    fun Account.toPostgresEntityMap(): MutableMap<String, *> {
        return mutableMapOf(
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
        val updatedAccount = db.save(account.toAccountEntity(isUpdated = true))
        log.info { "updated account: $updatedAccount" }
        updatedAccount.toAccount()
    }

    override suspend fun getAccountById(id: AccountId): Account? = withContext(Dispatchers.IO) {
        val account = db.findById(id.id)
        account?.toAccount().also { log.info { "found account: $account" } }
    }

    private companion object {
        private val log = KotlinLogging.logger { }
    }
}