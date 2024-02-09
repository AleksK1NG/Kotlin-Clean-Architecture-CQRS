package com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.services

import com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.events.*

class AccountEventsHandlerImpl : AccountEventsHandler {
    override suspend fun on(event: AccountCreatedEvent) {
        TODO("Not yet implemented")
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
}