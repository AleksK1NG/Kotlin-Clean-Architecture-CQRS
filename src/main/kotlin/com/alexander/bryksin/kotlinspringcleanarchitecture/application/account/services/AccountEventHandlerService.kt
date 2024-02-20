package com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.services

import com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.events.*

interface AccountEventHandlerService {

    suspend fun on(event: AccountCreatedEvent)

    suspend fun on(event: BalanceDepositedEvent)

    suspend fun on(event: BalanceWithdrawEvent)

    suspend fun on(event: PersonalInfoUpdatedEvent)

    suspend fun on(event: ContactInfoChangedEvent)

    suspend fun on(event: DomainStatusChangedEvent)
}