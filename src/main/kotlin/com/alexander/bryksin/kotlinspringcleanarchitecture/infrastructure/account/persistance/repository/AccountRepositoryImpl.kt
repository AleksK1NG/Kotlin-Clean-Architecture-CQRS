package com.alexander.bryksin.kotlinspringcleanarchitecture.infrastructure.account.persistance.repository

import com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.persistance.AccountRepository
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.models.Account
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.valueObjects.AccountId
import com.alexander.bryksin.kotlinspringcleanarchitecture.infrastructure.account.persistance.entity.toAccount
import com.alexander.bryksin.kotlinspringcleanarchitecture.infrastructure.account.persistance.entity.toAccountEntity
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.stereotype.Repository


@Repository
class AccountRepositoryImpl(private val db: AccountsCoroutineStore) : AccountRepository {

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