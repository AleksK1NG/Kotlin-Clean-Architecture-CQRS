package com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.services

import com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.commands.*
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.valueObjects.AccountId

interface AccountCommandService {

    suspend fun handle(command: CreateAccountCommand): AccountId

    suspend fun handle(command: ChangeAccountStatusCommand)

    suspend fun handle(command: ChangeContactInfoCommand)

    suspend fun handle(command: DepositBalanceCommand)

    suspend fun handle(command: WithdrawBalanceCommand)

    suspend fun handle(command: UpdatePersonalInfoCommand)
}