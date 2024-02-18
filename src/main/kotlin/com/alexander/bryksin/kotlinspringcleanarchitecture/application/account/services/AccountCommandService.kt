package com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.services

import com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.commands.*
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.models.Account

interface AccountCommandService {

    suspend fun handle(command: CreateAccountCommand): Account

    suspend fun handle(command: ChangeAccountStatusCommand): Account

    suspend fun handle(command: ChangeContactInfoCommand): Account

    suspend fun handle(command: DepositBalanceCommand): Account

    suspend fun handle(command: WithdrawBalanceCommand): Account

    suspend fun handle(command: UpdatePersonalInfoCommand): Account
}