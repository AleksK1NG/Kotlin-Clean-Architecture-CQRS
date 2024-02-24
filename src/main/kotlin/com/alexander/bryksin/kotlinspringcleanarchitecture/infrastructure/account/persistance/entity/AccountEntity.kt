package com.alexander.bryksin.kotlinspringcleanarchitecture.infrastructure.account.persistance.entity

import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.models.Account
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.valueObjects.*
import org.springframework.data.annotation.*
import org.springframework.data.domain.Persistable
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.io.Serializable
import java.time.Instant
import java.util.*


@Table(name = "accounts", schema = "microservices")
data class AccountEntity(
    @field:Id @field:Column("id") val accountId: UUID? = null,
    @field:Column("email") val email: String = "",
    @field:Column("phone") val phone: String = "",
    @field:Column("bio") val bio: String = "",
    @field:Column("image_url") val imageUrl: String = "",
    @field:Column("country") val country: String? = null,
    @field:Column("city") val city: String? = null,
    @field:Column("post_code") val postCode: String? = null,
    @field:Column("balance_amount") val amount: Long = 0,
    @field:Column("balance_currency") val balanceCurrency: BalanceCurrency = BalanceCurrency.USD,
    @field:Column("status") val status: AccountStatus = AccountStatus.FREE,

    @field:Version @field:Column("version") val version: Long = 0,
    @LastModifiedDate @field:Column("updated_at") val updatedAt: Instant? = null,
    @CreatedDate @field:Column("created_at") val createdAt: Instant? = null,
) : Serializable, Persistable<UUID> {

    @field:Transient
    var isUpdated: Boolean = false

    fun updated(updated: Boolean): AccountEntity {
        isUpdated = updated
        return this
    }

    override fun getId(): UUID? {
        return accountId
    }

    override fun isNew(): Boolean {
        return !isUpdated
    }

    companion object {}
}

fun AccountEntity.toAccount(): Account {
    val accountId = if (this.accountId != null) AccountId(this.accountId) else null

    return Account(
        accountId = accountId ?: AccountId(),
        contactInfo = ContactInfo(
            email = this.email,
            phone = this.phone,
        ),
        personalInfo = PersonalInfo(bio = this.bio, imageUrl = this.imageUrl),
        address = Address(
            country = this.country,
            city = this.city,
            postCode = this.postCode,
        ),
        balance = Balance(
            amount = this.amount,
            balanceCurrency = this.balanceCurrency,
        ),
        status = this.status,
        version = this.version,
        updatedAt = this.updatedAt,
        createdAt = this.createdAt,
    )
}


fun Account.toAccountEntity(isUpdated: Boolean = false): AccountEntity = AccountEntity(
    accountId = this.accountId?.id,
    email = this.contactInfo.email,
    phone = this.contactInfo.phone,
    bio = this.personalInfo.bio,
    imageUrl = this.personalInfo.imageUrl,
    country = this.address.country,
    city = this.address.city,
    postCode = this.address.postCode,
    amount = this.balance.amount,
    balanceCurrency = this.balance.balanceCurrency,
    status = this.status,
    version = this.version,
    updatedAt = this.updatedAt,
    createdAt = this.createdAt,
).updated(isUpdated)

