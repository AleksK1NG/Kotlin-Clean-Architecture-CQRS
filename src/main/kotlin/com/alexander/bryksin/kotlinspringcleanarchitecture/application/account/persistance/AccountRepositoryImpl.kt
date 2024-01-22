package com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.persistance

import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.models.Account
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.valueObjects.AccountId
import com.alexander.bryksin.kotlinspringcleanarchitecture.infrastructure.account.persistance.AccountRepository
import org.springframework.stereotype.Repository


@Repository
class AccountRepositoryImpl : AccountRepository {
    override suspend fun createAccount(account: Account): Account {
        TODO("Not yet implemented")
    }

    override suspend fun updateAccount(id: AccountId, account: Account): Account {
        TODO("Not yet implemented")
    }

    override suspend fun getAccountById(id: AccountId): Account {
        TODO("Not yet implemented")
    }
}