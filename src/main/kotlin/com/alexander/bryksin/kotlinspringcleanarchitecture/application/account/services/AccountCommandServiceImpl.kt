package com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.services

import com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.commands.*
import com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.events.AccountCreatedEvent
import com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.events.of
import com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.events.toOutboxEvent
import com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.persistance.AccountRepository
import com.alexander.bryksin.kotlinspringcleanarchitecture.application.common.publisher.OutboxPublisher
import com.alexander.bryksin.kotlinspringcleanarchitecture.application.common.serializer.Serializer
import com.alexander.bryksin.kotlinspringcleanarchitecture.application.outbox.persistance.OutboxRepository
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.models.Account
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.outbox.models.OutboxEvent
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
) : AccountCommandService {

    private val publisherScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override suspend fun handle(command: CreateAccountCommand): Account = withContext(Dispatchers.IO) {
        val (savedAccount, event) = tx.executeAndAwait {
            val savedAccount = accountRepository.createAccount(command.toAccount())
            log.info { "saved account: $savedAccount" }
            val domainEvent = AccountCreatedEvent.of(account = savedAccount)
            val outboxEvent = outboxRepository.insert(domainEvent.toOutboxEvent(serializer.serializeToBytes(domainEvent)))
            Pair(savedAccount, outboxEvent)
        }

        // Publish event
//        publishOutboxEvent(event)
        publisherScope.launch { publishOutboxEvent(event) }
        savedAccount
    }


    override suspend fun handle(command: ChangeAccountStatusCommand): Account = withContext(Dispatchers.IO) {
        tx.executeAndAwait {
            val account = accountRepository.getAccountById(command.accountId)
                ?: throw AccountNotFoundException(command.accountId.string())

            val updated = accountRepository.updateAccount(account.copy(status = command.status))
            log.info { "updated account: $updated" }
            updated
        }
    }

    override suspend fun handle(command: ChangeContactInfoCommand): Account {
        TODO("Not yet implemented")
    }

    override suspend fun handle(command: DepositBalanceCommand): Account {
        TODO("Not yet implemented")
    }

    override suspend fun handle(command: WithdrawBalanceCommand): Account {
        TODO("Not yet implemented")
    }

    override suspend fun handle(command: UpdatePersonalInfoCommand): Account {
        TODO("Not yet implemented")
    }

    private suspend fun publishOutboxEvent(event: OutboxEvent) = withContext(Dispatchers.IO) {
        try {
            outboxRepository.deleteWithLock(event) { outboxPublisher.publish(event) }
        } catch (e: Exception) {
            log.error(e) { "Error while publishing outbox event: ${event.eventId}, error: ${e.message}" }
        }
    }

    private companion object {
        private val log = KotlinLogging.logger { }
    }
}