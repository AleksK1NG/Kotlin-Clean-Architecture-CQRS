package com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.services

import com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.commands.*
import com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.persistance.AccountRepository
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.models.Account
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.stereotype.Service
import org.springframework.transaction.reactive.TransactionalOperator
import org.springframework.transaction.reactive.executeAndAwait
import javax.security.auth.login.AccountNotFoundException

@Service
class AccountCommandServiceImpl(
    private val accountRepository: AccountRepository,
    private val tx: TransactionalOperator
) : AccountCommandService {

    override suspend fun handle(command: CreateAccountCommand): Account = withContext(Dispatchers.IO) {
        tx.executeAndAwait {
            val savedAccount = accountRepository.createAccount(command.toAccount())
            log.info { "saved account: $savedAccount" }
            savedAccount
        }
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

    private companion object {
        private val log = KotlinLogging.logger { }
    }
}