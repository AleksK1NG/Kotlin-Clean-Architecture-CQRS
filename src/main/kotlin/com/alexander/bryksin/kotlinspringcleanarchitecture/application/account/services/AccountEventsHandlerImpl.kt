package com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.services

import com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.events.*
import com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.exceptions.InvalidEventVersionException
import com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.persistance.AccountMongoRepository
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.models.Account
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.valueObjects.AccountId
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.*
import org.springframework.stereotype.Component
import javax.security.auth.login.AccountNotFoundException
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext


@Component
class AccountEventsHandlerImpl(
    private val accountMongoRepository: AccountMongoRepository
) : AccountEventsHandler {

    override suspend fun on(event: AccountCreatedEvent): Unit = serviceScope {
        log.info { "AccountEventsHandlerImpl Account created event: $event" }
        accountMongoRepository.createAccount(event.toAccount())
    }

    override suspend fun on(event: BalanceDepositedEvent): Unit = serviceScope {
        val foundAccount = findAndValidateVersion(event.accountId!!, event.version)

        val accountToUpdate = foundAccount.copy(balance = event.balance, updatedAt = foundAccount.updatedAt)
        val updatedAccount = accountMongoRepository.updateAccount(accountToUpdate)
        log.info { "AccountEventsHandlerImpl BalanceDepositedEvent updatedAccount: $updatedAccount" }
    }


    override suspend fun on(event: BalanceWithdrawEvent) = serviceScope {
        val foundAccount = findAndValidateVersion(event.accountId!!, event.version)
        val accountToUpdate = foundAccount.copy(balance = event.balance, updatedAt = foundAccount.updatedAt)

        val updatedAccount = accountMongoRepository.updateAccount(accountToUpdate)
        log.info { "AccountEventsHandlerImpl BalanceWithdrawEvent updatedAccount: $updatedAccount" }
    }

    override suspend fun on(event: PersonalInfoUpdatedEvent) = serviceScope {
        val foundAccount = findAndValidateVersion(event.accountId!!, event.version)
        val accountToUpdate = foundAccount.copy(personalInfo = event.personalInfo, updatedAt = foundAccount.updatedAt)

        val updatedAccount = accountMongoRepository.updateAccount(accountToUpdate)
        log.info { "AccountEventsHandlerImpl PersonalInfoUpdatedEvent updatedAccount: $updatedAccount" }
    }


    override suspend fun on(event: ContactInfoChangedEvent) = serviceScope {
        val foundAccount = findAndValidateVersion(event.accountId!!, event.version)
        val accountToUpdate = foundAccount.copy(contactInfo = event.contactInfo, updatedAt = foundAccount.updatedAt)

        val updatedAccount = accountMongoRepository.updateAccount(accountToUpdate)
        log.info { "AccountEventsHandlerImpl PersonalInfoUpdatedEvent updatedAccount: $updatedAccount" }
    }

    override suspend fun on(event: AccountStatusChangedEvent): Unit = serviceScope {
        findAndUpdateAccountById(event.accountId!!, event.version) { foundAccount ->
            foundAccount.copy(status = event.status, updatedAt = foundAccount.updatedAt)
        }.also { log.info { "AccountEventsHandlerImpl AccountStatusChangedEvent updatedAccount: $it" } }
    }

    private suspend fun findAndUpdateAccountById(
        accountId: AccountId,
        eventVersion: Long,
        block: suspend (Account) -> Account
    ): Account {
        val foundAccount = findAndValidateVersion(accountId, eventVersion)
        val accountToUpdate = block(foundAccount)
        return accountMongoRepository.updateAccount(accountToUpdate)
    }

    private fun validateVersion(account: Account, eventVersion: Long) {
        log.warn { "invalid version: eventVersion: $eventVersion, accountVersion: ${account.version}" }
        throw InvalidEventVersionException(account.accountId, account.version, eventVersion)
    }

    private suspend fun findAndValidateVersion(accountId: AccountId, eventVersion: Long): Account {
        val foundAccount = accountMongoRepository.getAccountById(accountId)
            ?: throw AccountNotFoundException(accountId.string())

        validateVersion(foundAccount, eventVersion)
        return foundAccount
    }

    private val scope = CoroutineScope(Job() + CoroutineName(this::class.java.name) + Dispatchers.IO)

    private suspend fun <T> serviceScope(
        context: CoroutineContext = EmptyCoroutineContext,
        block: suspend (CoroutineScope) -> T
    ): T = block(scope + context)

    private companion object {
        private val log = KotlinLogging.logger { }
    }
}