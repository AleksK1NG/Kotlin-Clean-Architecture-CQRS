package com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.services

import com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.persistance.AccountProjectionRepository
import com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.persistance.AccountRepository
import com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.queries.GetAccountByEmailQuery
import com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.queries.GetAccountByIdQuery
import com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.queries.GetAllAccountsQuery
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.models.Account
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.valueObjects.AccountId
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import org.springframework.stereotype.Service
import javax.security.auth.login.AccountNotFoundException
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext


@Service
class AccountQueryServiceImpl(
    private val accountRepository: AccountRepository,
    private val accountProjectionRepository: AccountProjectionRepository
) : AccountQueryService {

    override suspend fun handle(query: GetAccountByIdQuery): Account = serviceScope {
        accountRepository.getAccountById(AccountId(query.id)) ?: throw AccountNotFoundException(query.id.toString())
    }

    override suspend fun handle(query: GetAccountByEmailQuery): Account = serviceScope {
        accountProjectionRepository.getAccountByEmail(query.email) ?: throw AccountNotFoundException(query.email)
    }

    override suspend fun handle(query: GetAllAccountsQuery): Flow<Account> = serviceScope {
        accountProjectionRepository.getAllAccounts(page = query.page, size = query.size)
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