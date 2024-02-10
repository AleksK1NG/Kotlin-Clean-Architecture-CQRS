package com.alexander.bryksin.kotlinspringcleanarchitecture.infrastructure.account.persistance.repository

import com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.persistance.AccountProjectionRepository
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.models.Account
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.valueObjects.AccountId
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Component


@Component
class AccountProjectionRepositoryImpl(private val databaseClient: DatabaseClient) : AccountProjectionRepository {

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