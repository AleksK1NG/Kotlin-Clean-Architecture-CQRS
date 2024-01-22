package com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.services

import com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.commands.CreateAccountCommand
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.models.Account
import com.alexander.bryksin.kotlinspringcleanarchitecture.infrastructure.account.persistance.AccountRepository
import org.springframework.stereotype.Service


@Service
class AccountCommandServiceImpl(
    private val accountRepository: AccountRepository,
) : AccountCommandService {

    override suspend fun handle(command: CreateAccountCommand): Account {
        TODO("Not yet implemented")
    }
}