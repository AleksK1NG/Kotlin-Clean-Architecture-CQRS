package com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.services

import com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.commands.*
import com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.events.*
import com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.exceptions.AccountNotFoundException
import com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.persistance.AccountRepository
import com.alexander.bryksin.kotlinspringcleanarchitecture.application.common.clients.EmailVerifierClient
import com.alexander.bryksin.kotlinspringcleanarchitecture.application.common.clients.PaymentClient
import com.alexander.bryksin.kotlinspringcleanarchitecture.application.common.outbox.persistance.OutboxRepository
import com.alexander.bryksin.kotlinspringcleanarchitecture.application.common.publisher.EventPublisher
import com.alexander.bryksin.kotlinspringcleanarchitecture.application.common.serializer.Serializer
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.models.Account
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.valueObjects.AccountId
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.common.outbox.models.OutboxEvent
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.common.utils.runSuspendCatching
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

    override suspend fun handle(command: CreateAccountCommand): AccountId = serviceScope {
        emailVerifierClient.verifyEmail(command.contactInfo.email)

        val (account, event) = tx.executeAndAwait {
            val account = accountRepository.saveAccount(command.toAccount())
            val event = outboxRepository.insert(account.toAccountCreatedEvent().toOutboxEvent(serializer))
            account to event
        }

        publisherScope.launch { publishOutboxEvent(event) }
        account.accountId
    }


    override suspend fun handle(command: ChangeAccountStatusCommand): Unit = serviceScope {
        val event = tx.executeAndAwait {
            val foundAccount = getAccountById(command.accountId)
            val account = accountRepository.updateAccount(foundAccount.updateStatus(command.status))
            val event = account.toStatusChangedEvent().toOutboxEvent(serializer)
            outboxRepository.insert(event)
        }

        publisherScope.launch { publishOutboxEvent(event) }
    }

    override suspend fun handle(command: ChangeContactInfoCommand): Unit = serviceScope {
        val event = tx.executeAndAwait {
            val foundAccount = getAccountById(command.accountId)
            val account = accountRepository.updateAccount(foundAccount.changeContactInfo(command.contactInfo))
            val event = account.toContactInfoChangedEvent().toOutboxEvent(serializer)
            outboxRepository.insert(event)
        }

        publisherScope.launch { publishOutboxEvent(event) }
    }

    override suspend fun handle(command: DepositBalanceCommand): Unit = serviceScope {
        paymentClient.verifyPaymentTransaction(command.accountId.string(), command.transactionId)

        val event = tx.executeAndAwait {
            val foundAccount = getAccountById(command.accountId)
            val account = accountRepository.updateAccount(foundAccount.depositBalance(command.balance))
            val event = account.toBalanceDepositedEvent(command.balance).toOutboxEvent(serializer)
            outboxRepository.insert(event)
        }

        publisherScope.launch { publishOutboxEvent(event) }
    }

    override suspend fun handle(command: WithdrawBalanceCommand): Unit = serviceScope {
        paymentClient.verifyPaymentTransaction(command.accountId.string(), command.transactionId)

        val event = tx.executeAndAwait {
            val foundAccount = getAccountById(command.accountId)
            val account = accountRepository.updateAccount(foundAccount.withdrawBalance(command.balance))
            val event = account.toBalanceWithdrawEvent(command.balance).toOutboxEvent(serializer)
            outboxRepository.insert(event)
        }

        publisherScope.launch { publishOutboxEvent(event) }
    }

    override suspend fun handle(command: UpdatePersonalInfoCommand): Unit = serviceScope {
        val event = tx.executeAndAwait {
            val foundAccount = getAccountById(command.accountId)
            val account = accountRepository.updateAccount(foundAccount.changePersonalInfo(command.personalInfo))
            val event = account.toPersonalInfoUpdatedEvent().toOutboxEvent(serializer)
            outboxRepository.insert(event)
        }

        publisherScope.launch { publishOutboxEvent(event) }
    }


    private suspend fun publishOutboxEvent(event: OutboxEvent) =
        runSuspendCatching { outboxRepository.deleteWithLock(event) { eventPublisher.publish(event) } }
            .onFailure {
                log.error { "Error while publishing outbox event: ${event.eventId}, error: ${it.message}" }
            }
            .onSuccess { log.info { "outbox event has been published and deleted: $it" } }

    private suspend fun getAccountById(accountId: AccountId): Account =
        accountRepository.getAccountById(accountId)
            ?: throw AccountNotFoundException(accountId)


    private val scope = CoroutineScope(Job() + CoroutineName(this::class.java.name) + Dispatchers.IO)
    private val publisherScope = CoroutineScope(SupervisorJob() + Dispatchers.IO + CoroutineName(this::class.java.name))

    private suspend fun <T> serviceScope(
        context: CoroutineContext = EmptyCoroutineContext,
        block: suspend CoroutineScope.() -> T
    ): T = block(scope + context)

    private companion object {
        private val log = KotlinLogging.logger { }
    }
}

