package com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.services

import com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.events.*
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.coroutineScope
import org.springframework.stereotype.Component


@Component
class AccountEventsHandlerImpl : AccountEventsHandler {

    override suspend fun on(event: AccountCreatedEvent) = coroutineScope {
        log.info { "AccountEventsHandlerImpl Account created event: $event" }
    }

    override suspend fun on(event: BalanceDepositedEvent) {
        TODO("Not yet implemented")
    }

    override suspend fun on(event: BalanceWithdrawEvent) {
        TODO("Not yet implemented")
    }

    override suspend fun on(event: PersonalInfoUpdatedEvent) {
        TODO("Not yet implemented")
    }

    override suspend fun on(event: ContactInfoChangedEvent) {
        TODO("Not yet implemented")
    }

    override suspend fun on(event: AccountStatusChangedEvent) {
        TODO("Not yet implemented")
    }


    private companion object {
        private val log = KotlinLogging.logger { }
    }
}