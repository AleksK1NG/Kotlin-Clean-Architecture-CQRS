package com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.services

import com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.commands.CreateAccountCommand
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.models.Account

interface AccountCommandService {
    suspend fun handle(command: CreateAccountCommand): Account
}