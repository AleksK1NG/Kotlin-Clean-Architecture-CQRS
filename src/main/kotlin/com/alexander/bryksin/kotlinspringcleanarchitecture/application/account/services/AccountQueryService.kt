package com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.services

import com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.queries.GetAccountByEmailQuery
import com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.queries.GetAccountByIdQuery
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.models.Account

interface AccountQueryService {
    suspend fun handle(query: GetAccountByIdQuery): Account
    suspend fun handle(query: GetAccountByEmailQuery): Account
}