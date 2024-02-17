package com.alexander.bryksin.kotlinspringcleanarchitecture.infrastructure.account.persistance.entity

import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.models.Account
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.valueObjects.*
import com.mongodb.client.model.Updates
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId
import java.time.Instant
import java.util.*

data class AccountDocument(
    @field:BsonId
    val id: ObjectId,
    val accountId: String?,
    val contactInfo: ContactInfo = ContactInfo(),
    val personalInfo: PersonalInfo = PersonalInfo(),
    val address: Address = Address(),
    val balance: Balance = Balance(),
    val status: AccountStatus = AccountStatus.FREE,
    val version: Long = 0,
    val updatedAt: Instant? = null,
    val createdAt: Instant? = null,
) {
    companion object {}
}

fun Account.toDocument(): AccountDocument = AccountDocument(
    id = ObjectId(),
    accountId = accountId?.id.toString(),
    contactInfo = contactInfo,
    personalInfo = personalInfo,
    address = address,
    balance = balance,
    status = status,
    version = version,
    updatedAt = updatedAt,
    createdAt = createdAt,
)

fun AccountDocument.toAccount(): Account = Account(
    accountId = AccountId(id = UUID.fromString(accountId)),
    contactInfo = contactInfo,
    personalInfo = personalInfo,
    address = address,
    balance = balance,
    status = status,
    version = version,
    updatedAt = updatedAt,
    createdAt = createdAt,
)


fun Account.toBsonUpdate() = Updates.combine(
    Updates.set("contactInfo.email", contactInfo.email),
    Updates.set("contactInfo.phone", contactInfo.phone),
    Updates.set("personalInfo.bio", personalInfo.bio),
    Updates.set("personalInfo.imageUrl", personalInfo.imageUrl),
    Updates.set("address.city", address.city),
    Updates.set("address.country", address.country),
    Updates.set("address.postCode", address.postCode),
    Updates.set("status", status),
    Updates.set("version", version),
    Updates.set("balance.amount", balance.amount),
    Updates.set("balance.balanceCurrency", balance.balanceCurrency),
    Updates.set("updatedAt", updatedAt),
)