package com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.events

import com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.events.ContactInfoChangedEvent.Companion.ACCOUNT_CONTACT_INFO_CHANGED_V1
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.models.Account
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.valueObjects.AccountId
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.valueObjects.ContactInfo
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.common.outbox.models.OutboxEvent
import java.time.Instant
import java.util.*

data class ContactInfoChangedEvent(
    val accountId: AccountId?,
    val contactInfo: ContactInfo = ContactInfo(),
    val version: Long = 0,
    val updatedAt: Instant? = null,
    val createdAt: Instant? = null,
) : AccountEvent{
    companion object {
        const val ACCOUNT_CONTACT_INFO_CHANGED_V1 = "ACCOUNT_CONTACT_INFO_CHANGED_V1"
    }
}


fun Account.toContactInfoChangedEvent() = ContactInfoChangedEvent(
    accountId = this.accountId,
    contactInfo = this.contactInfo,
    version = this.version,
    updatedAt = this.updatedAt,
    createdAt = this.createdAt,
)

fun ContactInfoChangedEvent.toOutboxEvent(data: ByteArray): OutboxEvent = OutboxEvent(
    eventId = UUID.randomUUID(),
    eventType = ACCOUNT_CONTACT_INFO_CHANGED_V1,
    aggregateId = this.accountId?.id.toString(),
    data = data,
    version = this.version,
    timestamp = Instant.now(),
)