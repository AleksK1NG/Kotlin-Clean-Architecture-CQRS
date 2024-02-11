package com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.services

import com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.commands.*
import com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.dto.AccountWithEvent
import com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.events.*
import com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.exceptions.InvalidTransactionException
import com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.persistance.AccountRepository
import com.alexander.bryksin.kotlinspringcleanarchitecture.application.common.outbox.persistance.OutboxRepository
import com.alexander.bryksin.kotlinspringcleanarchitecture.application.common.publisher.OutboxPublisher
import com.alexander.bryksin.kotlinspringcleanarchitecture.application.common.serializer.Serializer
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.exceptions.EmailVerificationException
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.models.Account
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.valueObjects.AccountId
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.common.outbox.models.OutboxEvent
import com.alexander.bryksin.kotlinspringcleanarchitecture.infrastructure.account.clients.EmailVerifierClient
import com.alexander.bryksin.kotlinspringcleanarchitecture.infrastructure.account.clients.PaymentClient
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.*
import org.springframework.stereotype.Service
import org.springframework.transaction.reactive.TransactionalOperator
import org.springframework.transaction.reactive.executeAndAwait
import javax.security.auth.login.AccountNotFoundException

@Service
class AccountCommandServiceImpl(
    private val accountRepository: AccountRepository,
    private val outboxRepository: OutboxRepository,
    private val tx: TransactionalOperator,
    private val outboxPublisher: OutboxPublisher,
    private val serializer: Serializer,
    private val emailVerifierClient: EmailVerifierClient,
    private val paymentClient: PaymentClient
) : AccountCommandService {

    override suspend fun handle(command: CreateAccountCommand): Account = withContext(Dispatchers.IO) {
        validateAndVerifyEmail(command.email)

        val (savedAccount, event) = tx.executeAndAwait {
            val savedAccount = accountRepository.createAccount(command.toAccount())
            log.info { "saved account: $savedAccount" }
            val domainEvent = AccountCreatedEvent.of(account = savedAccount)
            val outboxEvent = outboxRepository.insert(
                domainEvent.toOutboxEvent(serializer.serializeToBytes(domainEvent))
            )
            AccountWithEvent(savedAccount, outboxEvent)
        }

//        publisherScope.launch { publishOutboxEvent(event) }
        savedAccount
    }


    override suspend fun handle(command: ChangeAccountStatusCommand): Account = withContext(Dispatchers.IO) {
        val (account, event) = tx.executeAndAwait {
            val account = getAccountById(command.accountId)

            val updatedAccount = accountRepository.updateAccount(account.copy(status = command.status))
            log.info { "updated account: $updatedAccount" }

            val domainEvent = updatedAccount.toStatusChangedEvent()
            val outboxEvent = outboxRepository.insert(
                domainEvent.toOutboxEvent(serializer.serializeToBytes(domainEvent))
            )

            AccountWithEvent(updatedAccount, outboxEvent)
        }

        publisherScope.launch { publishOutboxEvent(event) }
        account
    }

    override suspend fun handle(command: ChangeContactInfoCommand): Account = withContext(Dispatchers.IO) {
        val (account, event) = tx.executeAndAwait {
            val account = getAccountById(command.accountId)

            val updatedAccount = accountRepository.updateAccount(account.copy(contactInfo = command.contactInfo))
            log.info { "updated account: $updatedAccount" }

            val domainEvent = updatedAccount.toContactInfoChangedEvent()
            val outboxEvent = outboxRepository.insert(
                domainEvent.toOutboxEvent(serializer.serializeToBytes(domainEvent))
            )

            AccountWithEvent(updatedAccount, outboxEvent)
        }

        publisherScope.launch { publishOutboxEvent(event) }
        account
    }

    override suspend fun handle(command: DepositBalanceCommand): Account = withContext(Dispatchers.IO) {

        validateTransaction(command.accountId, command.transactionId)

        val (account, event) = tx.executeAndAwait {
            val account = getAccountById(command.accountId)

            val updatedAccount = accountRepository.updateAccount(account.depositBalance(command.balance.amount))
            log.info { "updated account: $updatedAccount" }

            val domainEvent = updatedAccount.toBalanceDepositedEvent()
            val outboxEvent = outboxRepository.insert(
                domainEvent.toOutboxEvent(serializer.serializeToBytes(domainEvent))
            )

            AccountWithEvent(updatedAccount, outboxEvent)
        }

        publisherScope.launch { publishOutboxEvent(event) }
        account
    }

    override suspend fun handle(command: WithdrawBalanceCommand): Account = withContext(Dispatchers.IO) {

        validateTransaction(command.accountId, command.transactionId)

        val (account, event) = tx.executeAndAwait {
            val account = getAccountById(command.accountId)

            val updatedAccount = accountRepository.updateAccount(account.withdrawBalance(command.balance.amount))
            log.info { "updated account: $updatedAccount" }

            val domainEvent = updatedAccount.toBalanceWithdrawEvent()
            val outboxEvent = outboxRepository.insert(
                domainEvent.toOutboxEvent(serializer.serializeToBytes(domainEvent))
            )

            AccountWithEvent(updatedAccount, outboxEvent)
        }

        publisherScope.launch { publishOutboxEvent(event) }
        account
    }

    override suspend fun handle(command: UpdatePersonalInfoCommand): Account = withContext(Dispatchers.IO) {
        val (account, event) = tx.executeAndAwait {
            val account = getAccountById(command.accountId)
            account.changePersonalInfo(command.personalInfo)

            val updatedAccount = accountRepository.updateAccount(account.changePersonalInfo(command.personalInfo))
            log.info { "updated account: $updatedAccount" }

            val domainEvent = updatedAccount.toPersonalInfoUpdatedEvent()
            val outboxEvent = outboxRepository.insert(
                domainEvent.toOutboxEvent(serializer.serializeToBytes(domainEvent))
            )

            AccountWithEvent(updatedAccount, outboxEvent)
        }

        publisherScope.launch { publishOutboxEvent(event) }
        account
    }

    private suspend fun publishOutboxEvent(event: OutboxEvent) = withContext(Dispatchers.IO) {
        try {
            outboxRepository.deleteWithLock(event) { outboxPublisher.publish(event) }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            log.error { "Error while publishing outbox event: ${event.eventId}, error: ${e.message}" }
        }
    }

    private val publisherScope = CoroutineScope(
        SupervisorJob() + Dispatchers.IO + CoroutineName("AccountCommandServicePublisher")
    )

    private suspend fun validateTransaction(accountId: AccountId, transactionId: String) {
        if (!paymentClient.verifyPaymentTransaction(
                accountId = accountId.string(),
                transactionId = transactionId
            )
        ) throw InvalidTransactionException(transactionId, accountId)
    }

    private suspend fun validateAndVerifyEmail(email: String) {
        if (emailVerifierClient.verifyEmail(email)) throw EmailVerificationException(email)
    }

    private suspend fun getAccountById(accountId: AccountId): Account =
        accountRepository.getAccountById(accountId) ?: throw AccountNotFoundException(accountId.string())

    private companion object {
        private val log = KotlinLogging.logger { }
    }
}

