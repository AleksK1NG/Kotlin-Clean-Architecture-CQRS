package com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.services

import com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.events.*
import com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.persistance.AccountProjectionRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.*
import org.springframework.stereotype.Component
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext


@Component
class AccountEventsHandlerImpl(
    private val accountCommandService: AccountCommandService,
    private val accountQueryService: AccountQueryService,
    private val accountProjectionRepository: AccountProjectionRepository
) : AccountEventsHandler {

    override suspend fun on(event: AccountCreatedEvent): Unit = serviceScope {
        log.info { "AccountEventsHandlerImpl Account created event: $event" }
        accountProjectionRepository.createAccount(event.toAccount())
    }

    override suspend fun on(event: BalanceDepositedEvent) = serviceScope {
        log.info { "AccountEventsHandlerImpl BalanceDepositedEvent event: $event" }
    }

    override suspend fun on(event: BalanceWithdrawEvent) = serviceScope {
        log.info { "AccountEventsHandlerImpl BalanceWithdrawEvent event: $event" }
    }

    override suspend fun on(event: PersonalInfoUpdatedEvent) = serviceScope {
        log.info { "AccountEventsHandlerImpl PersonalInfoUpdatedEvent event: $event" }
    }

    override suspend fun on(event: ContactInfoChangedEvent) = serviceScope {
        log.info { "AccountEventsHandlerImpl ContactInfoChangedEvent event: $event" }
    }

    override suspend fun on(event: AccountStatusChangedEvent) = serviceScope {
        log.info { "AccountEventsHandlerImpl AccountStatusChangedEvent event: $event" }
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