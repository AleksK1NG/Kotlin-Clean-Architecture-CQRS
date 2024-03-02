package com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.events

import com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.events.AccountCreatedEvent.Companion.ACCOUNT_CREATED_EVENT_V1
import com.alexander.bryksin.kotlinspringcleanarchitecture.application.common.serializer.Serializer
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.models.Account
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.valueObjects.AccountId
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.valueObjects.Address
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.valueObjects.ContactInfo
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.valueObjects.PersonalInfo
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.common.outbox.models.OutboxEvent
import java.time.Instant
import java.util.*

data class AccountCreatedEvent(
    val accountId: AccountId,
    val contactInfo: ContactInfo = ContactInfo(),
    val personalInfo: PersonalInfo = PersonalInfo(),
    val address: Address = Address(),
    val updatedAt: Instant? = null,
    val createdAt: Instant? = null,

    override val eventId: String,
    override val eventType: String,
    override val aggregateId: String,
    override val version: Long = 0,
    override val timestamp: Instant,
) : DomainEvent {
    companion object {
        const val ACCOUNT_CREATED_EVENT_V1 = "ACCOUNT_CREATED_EVENT_V1"
    }
}

fun AccountCreatedEvent.toOutboxEvent(serializer: Serializer) = OutboxEvent(
    eventId = UUID.randomUUID(),
    eventType = ACCOUNT_CREATED_EVENT_V1,
    aggregateId = aggregateId,
    version = version,
    timestamp = Instant.now(),
    data = serializer.serializeToBytes(this)
)

fun AccountCreatedEvent.toAccount() = Account(
    accountId = AccountId(aggregateId.toUUID()),
    contactInfo = contactInfo,
    personalInfo = personalInfo,
    address = address,
    version = version,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun Account.toAccountCreatedEvent(): AccountCreatedEvent {
    return AccountCreatedEvent(
        accountId = accountId,
        aggregateId = accountId.id.toString(),
        contactInfo = contactInfo,
        personalInfo = personalInfo,
        address = address,
        version = version,
        updatedAt = updatedAt,
        createdAt = createdAt,

        eventId = UUID.randomUUID().toString(),
        eventType = ACCOUNT_CREATED_EVENT_V1,
        timestamp = Instant.now()
    )
}


fun Account.toAccountCreatedOutboxEvent(serializer: Serializer): OutboxEvent {
    return this.toAccountCreatedEvent().toOutboxEvent(serializer)
}