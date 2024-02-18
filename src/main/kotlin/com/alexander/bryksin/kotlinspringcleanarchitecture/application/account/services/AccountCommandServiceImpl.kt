package com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.services

import com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.commands.*
import com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.events.*
import com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.exceptions.AccountNotFoundException
import com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.exceptions.InvalidTransactionException
import com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.persistance.AccountRepository
import com.alexander.bryksin.kotlinspringcleanarchitecture.application.common.clients.EmailVerifierClient
import com.alexander.bryksin.kotlinspringcleanarchitecture.application.common.clients.PaymentClient
import com.alexander.bryksin.kotlinspringcleanarchitecture.application.common.outbox.persistance.OutboxRepository
import com.alexander.bryksin.kotlinspringcleanarchitecture.application.common.publisher.EventPublisher
import com.alexander.bryksin.kotlinspringcleanarchitecture.application.common.serializer.Serializer
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.exceptions.EmailVerificationException
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.models.Account
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.valueObjects.AccountId
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.common.outbox.models.OutboxEvent
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.*
import org.springframework.stereotype.Service
import org.springframework.transaction.reactive.TransactionalOperator
import org.springframework.transaction.reactive.executeAndAwait
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

@Service
class AccountCommandServiceImpl(
    private val accountRepository: AccountRepository,
    private val outboxRepository: OutboxRepository,
    private val tx: TransactionalOperator,
    private val eventPublisher: EventPublisher,
    private val serializer: Serializer,
    private val emailVerifierClient: EmailVerifierClient,
    private val paymentClient: PaymentClient
) : AccountCommandService {

    override suspend fun handle(command: CreateAccountCommand): Account = serviceScope {
        validateAndVerifyEmail(command.contactInfo.email)

        val (account, event) = tx.executeAndAwait {
            val account = accountRepository.saveAccount(command.toAccount())
            val event = outboxRepository.insert(account.toAccountCreatedEvent().toOutboxEvent(serializer))
            account to event
        }

        publisherScope.launch { publishOutboxEvent(event) }
        account
    }


    override suspend fun handle(command: ChangeAccountStatusCommand): Account = serviceScope {
        val (account, event) = tx.executeAndAwait {
            val foundAccount = getAccountById(command.accountId)
            val account = accountRepository.updateAccount(foundAccount.updateStatus(command.status))
            val event = outboxRepository.insert(account.toStatusChangedEvent().toOutboxEvent(serializer))
            account to event
        }

        publisherScope.launch { publishOutboxEvent(event) }
        account
    }

    override suspend fun handle(command: ChangeContactInfoCommand): Account = serviceScope {
        val (account, event) = tx.executeAndAwait {
            val foundAccount = getAccountById(command.accountId)
            val account = accountRepository.updateAccount(foundAccount.changeContactInfo(command.contactInfo))
            val event = outboxRepository.insert(account.toContactInfoChangedEvent().toOutboxEvent(serializer))
            account to event
        }

        publisherScope.launch { publishOutboxEvent(event) }
        account
    }

    override suspend fun handle(command: DepositBalanceCommand): Account = serviceScope {
        validateTransaction(command.accountId, command.transactionId)

        val (account, event) = tx.executeAndAwait {
            val foundAccount = getAccountById(command.accountId)
            val account = accountRepository.updateAccount(foundAccount.depositBalance(command.balance.amount))
            val event = outboxRepository.insert(account.toBalanceDepositedEvent().toOutboxEvent(serializer))
            account to event
        }

        publisherScope.launch { publishOutboxEvent(event) }
        account
    }

    override suspend fun handle(command: WithdrawBalanceCommand): Account = serviceScope {
        validateTransaction(command.accountId, command.transactionId)

        val (account, event) = tx.executeAndAwait {
            val foundAccount = getAccountById(command.accountId)
            val account = accountRepository.updateAccount(foundAccount.withdrawBalance(command.balance.amount))
            val event = outboxRepository.insert(account.toBalanceWithdrawEvent().toOutboxEvent(serializer))
            account to event
        }

        publisherScope.launch { publishOutboxEvent(event) }
        account
    }

    override suspend fun handle(command: UpdatePersonalInfoCommand): Account = serviceScope {
        val (account, event) = tx.executeAndAwait {
            val foundAccount = getAccountById(command.accountId)
            val account = accountRepository.updateAccount(foundAccount.changePersonalInfo(command.personalInfo))
            val event = outboxRepository.insert(account.toPersonalInfoUpdatedEvent().toOutboxEvent(serializer))
            account to event
        }

        publisherScope.launch { publishOutboxEvent(event) }
        account
    }

    private suspend fun publishOutboxEvent(event: OutboxEvent) = serviceScope {
        try {
            outboxRepository.deleteWithLock(event) { eventPublisher.publish(event) }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            log.error { "Error while publishing outbox event: ${event.eventId}, error: ${e.message}" }
        }
    }

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
        accountRepository.getAccountById(accountId) ?: throw AccountNotFoundException(accountId)


    private val scope = CoroutineScope(Job() + CoroutineName(this::class.java.name) + Dispatchers.IO)
    private val publisherScope = CoroutineScope(SupervisorJob() + Dispatchers.IO + CoroutineName(this::class.java.name))

    private suspend fun <T> serviceScope(
        context: CoroutineContext = EmptyCoroutineContext,
        block: suspend (CoroutineScope) -> T
    ): T = block(scope + context)

    private companion object {
        private val log = KotlinLogging.logger { }


    }
}

