package com.alexander.bryksin.kotlinspringcleanarchitecture.infrastructure.account.persistance.entity

import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.valueObjects.AccountStatus
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.valueObjects.BalanceCurrency
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Version
import java.time.OffsetDateTime
import java.util.*

data class AccountEntity(
    @field:Id val accountId: UUID,
    val email: String = "",
    val phone: String = "",
    val bio: String = "",
    val imageUrl: String = "",
    val country: String? = null,
    val city: String? = null,
    val postCode: String? = null,
    val amount: Long = 0,
    val balanceCurrency: BalanceCurrency = BalanceCurrency.USD,
    val status: AccountStatus = AccountStatus.FREE,

    @Version
    val version: Long = 0,
    val updatedAt: OffsetDateTime? = null,
    val createdAt: OffsetDateTime? = null,
)
