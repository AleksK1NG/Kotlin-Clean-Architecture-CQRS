package com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.services

import arrow.core.Either
import com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.models.AccountsList
import com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.persistance.AccountProjectionRepository
import com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.persistance.AccountRepository
import com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.queries.GetAccountByEmailQuery
import com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.queries.GetAccountByIdQuery
import com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.queries.GetAllAccountsQuery
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.errors.AppError
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.models.Account
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.valueObjects.AccountId
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.common.scope.eitherScope
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import org.springframework.stereotype.Service


@Service
class AccountQueryServiceImpl(
    private val accountRepository: AccountRepository,
    private val accountProjectionRepository: AccountProjectionRepository
) : AccountQueryService {

    override suspend fun handle(query: GetAccountByIdQuery): Either<AppError, Account> = eitherScope {
        accountRepository.getAccountById(AccountId(query.id)).bind()
    }

    override suspend fun handle(query: GetAccountByEmailQuery): Either<AppError, Account> = eitherScope {
        accountProjectionRepository.getAccountByEmail(query.email).bind()
    }

    override suspend fun handle(query: GetAllAccountsQuery): Either<AppError, AccountsList> = eitherScope {
        accountProjectionRepository.getAllAccounts(page = query.page, size = query.size).bind()
    }

    private val scope = CoroutineScope(Job() + CoroutineName(this::class.java.name) + Dispatchers.IO)

    private companion object {
        private val log = KotlinLogging.logger { }
    }
}