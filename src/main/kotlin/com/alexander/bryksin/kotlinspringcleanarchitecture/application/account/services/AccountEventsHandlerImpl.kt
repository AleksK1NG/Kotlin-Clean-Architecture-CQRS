package com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.services

import com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.events.*
import com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.exceptions.LowerEventVersionException
import com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.exceptions.SameEventVersionException
import com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.exceptions.UpperEventVersionException
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
        accountMongoRepository.createAccount(event.toAccount())
    }

    override suspend fun on(event: BalanceDepositedEvent): Unit = serviceScope {
        findAndUpdateAccountById(event.accountId!!, event.version) { foundAccount ->
            foundAccount.depositBalance(event.balance.amount)
        }
    }


    override suspend fun on(event: BalanceWithdrawEvent): Unit = serviceScope {
        findAndUpdateAccountById(event.accountId!!, event.version) { foundAccount ->
            foundAccount.withdrawBalance(event.balance.amount)
        }
    }

    override suspend fun on(event: PersonalInfoUpdatedEvent): Unit = serviceScope {
        findAndUpdateAccountById(event.accountId!!, event.version) { foundAccount ->
            foundAccount.changePersonalInfo(event.personalInfo)
        }
    }


    override suspend fun on(event: ContactInfoChangedEvent): Unit = serviceScope {
        findAndUpdateAccountById(event.accountId!!, event.version) { foundAccount ->
            foundAccount.changeContactInfo(event.contactInfo)
        }
    }

    override suspend fun on(event: AccountStatusChangedEvent): Unit = serviceScope {
        findAndUpdateAccountById(event.accountId!!, event.version) { foundAccount ->
            foundAccount.updateStatus(event.status)
        }
    }

    private suspend fun findAndUpdateAccountById(
        accountId: AccountId,
        eventVersion: Long,
        block: suspend (Account) -> Account
    ): Account {
        val foundAccount = findAndValidateVersion(accountId, eventVersion)
        val accountToUpdate = block(foundAccount)
        return accountMongoRepository.updateAccount(accountToUpdate)
            .also { log.info { "mongo repository updated account: $it" } }
    }

    private fun validateVersion(account: Account, eventVersion: Long) {
        when {
            eventVersion < account.version + 1 ->
                throw LowerEventVersionException(account.accountId, account.version, eventVersion)

            eventVersion == account.version + 1 ->
                throw SameEventVersionException(account.accountId, account.version, eventVersion)

            eventVersion > account.version + 1 ->
                throw UpperEventVersionException(account.accountId, account.version, eventVersion)
        }
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