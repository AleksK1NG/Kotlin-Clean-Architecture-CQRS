package com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.queries

import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.valueObjects.AccountId

data class GetAccountByIdQuery(val id: AccountId) : AccountDomainQuery
