package com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.services

import com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.queries.GetAccountByIdQuery
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.models.Account
import com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.persistance.AccountRepository
import org.springframework.stereotype.Service


@Service
class AccountQueryServiceImpl(
    private val accountRepository: AccountRepository,
) : AccountQueryService {
    override suspend fun handle(query: GetAccountByIdQuery): Account {
        TODO("Not yet implemented")
    }
}