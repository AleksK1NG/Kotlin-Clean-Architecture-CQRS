package com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.events

import com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.events.ContactInfoChangedEvent.Companion.ACCOUNT_CONTACT_INFO_CHANGED_V1
import com.alexander.bryksin.kotlinspringcleanarchitecture.application.common.serializer.Serializer
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.models.Account
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.valueObjects.AccountId
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.valueObjects.ContactInfo
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.outbox.models.OutboxEvent
import java.time.Instant
import java.util.*

data class ContactInfoChangedEvent(
    val accountId: AccountId,
    val contactInfo: ContactInfo = ContactInfo(),
    val updatedAt: Instant? = null,
    val createdAt: Instant? = null,

    override val eventId: String,
    override val eventType: String,
    override val aggregateId: String,
    override val version: Long = 0,
    override val timestamp: Instant,
) : DomainEvent {
    companion object {
        const val ACCOUNT_CONTACT_INFO_CHANGED_V1 = "ACCOUNT_CONTACT_INFO_CHANGED_V1"
    }
}


fun Account.toContactInfoChangedEvent(): ContactInfoChangedEvent {
    return ContactInfoChangedEvent(
        accountId = accountId,
        aggregateId = accountId.id.toString(),
        eventId = UUID.randomUUID().toString(),
        eventType = ACCOUNT_CONTACT_INFO_CHANGED_V1,
        timestamp = Instant.now(),
        version = version,

        contactInfo = contactInfo,
        updatedAt = updatedAt,
        createdAt = createdAt,
    )
}

fun Account.toContactInfoChangedOutboxEvent(serializer: Serializer): OutboxEvent {
    return this.toContactInfoChangedEvent().toOutboxEvent(serializer)
}


fun ContactInfoChangedEvent.toOutboxEvent(serializer: Serializer) = OutboxEvent(
    eventId = UUID.randomUUID(),
    eventType = ACCOUNT_CONTACT_INFO_CHANGED_V1,
    aggregateId = aggregateId,
    version = version,
    timestamp = Instant.now(),
    data = serializer.serializeToBytes(this)
)