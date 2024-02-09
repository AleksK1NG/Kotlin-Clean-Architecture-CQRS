package com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.services

import com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.commands.*
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.models.Account
import com.alexander.bryksin.kotlinspringcleanarchitecture.infrastructure.account.persistance.AccountRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service


@Service
class AccountCommandServiceImpl(
    private val accountRepository: AccountRepository,
) : AccountCommandService {

    override suspend fun handle(command: CreateAccountCommand): Account {
        TODO("Not yet implemented")
    }

    override suspend fun handle(command: ChangeAccountStatusCommand): Account {
        TODO("Not yet implemented")
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

    private companion object {
        private val log = KotlinLogging.logger { }
    }
}