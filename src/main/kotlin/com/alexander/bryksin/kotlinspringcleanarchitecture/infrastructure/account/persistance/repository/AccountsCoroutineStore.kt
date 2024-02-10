package com.alexander.bryksin.kotlinspringcleanarchitecture.infrastructure.account.persistance.repository

import com.alexander.bryksin.kotlinspringcleanarchitecture.infrastructure.account.persistance.entity.AccountEntity
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import java.util.*

interface AccountsCoroutineStore : CoroutineCrudRepository<AccountEntity, UUID> {
}