package com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.services

import arrow.core.Either
import com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.commands.*
import com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.events.*
import com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.persistance.AccountRepository
import com.alexander.bryksin.kotlinspringcleanarchitecture.application.common.clients.EmailVerifierClient
import com.alexander.bryksin.kotlinspringcleanarchitecture.application.common.clients.PaymentClient
import com.alexander.bryksin.kotlinspringcleanarchitecture.application.common.outbox.persistance.OutboxRepository
import com.alexander.bryksin.kotlinspringcleanarchitecture.application.common.publisher.EventPublisher
import com.alexander.bryksin.kotlinspringcleanarchitecture.application.common.serializer.Serializer
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.errors.AppError
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.valueObjects.AccountId
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.common.outbox.models.OutboxEvent
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.common.scope.eitherScope
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.common.utils.runSuspendCatching
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.*
import org.springframework.stereotype.Service
import org.springframework.transaction.reactive.TransactionalOperator
import org.springframework.transaction.reactive.executeAndAwait

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

    override suspend fun handle(command: CreateAccountCommand): Either<AppError, AccountId> = eitherScope(ctx) {
        emailVerifierClient.verifyEmail(command.contactInfo.email).bind()

        val (account, event) = tx.executeAndAwait {
            val account = accountRepository.save(command.toAccount()).bind()
            val event = outboxRepository.insert(account.toAccountCreatedEvent().toOutboxEvent(serializer)).bind()
            account to event
        }

        publisherScope.launch { publishOutboxEvent(event) }
        account.accountId
    }


    override suspend fun handle(command: ChangeAccountStatusCommand): Either<AppError, Unit> = eitherScope(ctx) {
        val event = tx.executeAndAwait {
            val foundAccount = accountRepository.getById(command.accountId).bind()
            foundAccount.updateStatus(command.status).bind()

            val account = accountRepository.update(foundAccount).bind()
            val event = account.toStatusChangedEvent().toOutboxEvent(serializer)
            outboxRepository.insert(event).bind()
        }

        publisherScope.launch { publishOutboxEvent(event) }
    }

    override suspend fun handle(command: ChangeContactInfoCommand): Either<AppError, Unit> = eitherScope(ctx) {
        val event = tx.executeAndAwait {
            val foundAccount = accountRepository.getById(command.accountId).bind()
            foundAccount.changeContactInfo(command.contactInfo).bind()

            val account = accountRepository.update(foundAccount).bind()
            val event = account.toContactInfoChangedEvent().toOutboxEvent(serializer)
            outboxRepository.insert(event).bind()
        }

        publisherScope.launch { publishOutboxEvent(event) }
    }

    override suspend fun handle(command: DepositBalanceCommand): Either<AppError, Unit> = eitherScope(ctx) {
        paymentClient.verifyPaymentTransaction(command.accountId.string(), command.transactionId).bind()

        val event = tx.executeAndAwait {
            val foundAccount = accountRepository.getById(command.accountId).bind()
            foundAccount.depositBalance(command.balance).bind()

            val account = accountRepository.update(foundAccount).bind()
            val event = account.toBalanceDepositedEvent(command.balance).toOutboxEvent(serializer)
            outboxRepository.insert(event).bind()
        }

        publisherScope.launch { publishOutboxEvent(event) }
    }

    override suspend fun handle(command: WithdrawBalanceCommand): Either<AppError, Unit> = eitherScope(ctx) {
        paymentClient.verifyPaymentTransaction(command.accountId.string(), command.transactionId).bind()

        val event = tx.executeAndAwait {
            val foundAccount = accountRepository.getById(command.accountId).bind()
            foundAccount.withdrawBalance(command.balance).bind()

            val account = accountRepository.update(foundAccount).bind()
            val event = account.toBalanceWithdrawEvent(command.balance).toOutboxEvent(serializer)
            outboxRepository.insert(event).bind()
        }

        publisherScope.launch { publishOutboxEvent(event) }
    }

    override suspend fun handle(command: UpdatePersonalInfoCommand): Either<AppError, Unit> = eitherScope(ctx) {
        val event = tx.executeAndAwait {
            val foundAccount = accountRepository.getById(command.accountId).bind()
            foundAccount.changePersonalInfo(command.personalInfo).bind()

            val account = accountRepository.update(foundAccount).bind()
            val event = account.toPersonalInfoUpdatedEvent().toOutboxEvent(serializer)
            outboxRepository.insert(event).bind()
        }

        publisherScope.launch { publishOutboxEvent(event) }
    }

    private suspend fun publishOutboxEvent(event: OutboxEvent) =
        runSuspendCatching { outboxRepository.deleteWithLock(event) { eventPublisher.publish(event) } }
            .onFailure {
                log.error { "error while publishing outbox event: ${event.eventId}, error: ${it.message}" }
            }
            .onSuccess { log.info { "outbox event has been published and deleted: $it" } }


    private val ctx = Job() + CoroutineName(this::class.java.name) + Dispatchers.IO
    private val publisherScope = CoroutineScope(SupervisorJob() + Dispatchers.IO + CoroutineName(this::class.java.name))

    private companion object {
        private val log = KotlinLogging.logger { }
    }
}

