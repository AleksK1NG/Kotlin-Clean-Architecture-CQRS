package com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.events

import com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.events.PersonalInfoUpdatedEvent.Companion.ACCOUNT_PERSONAL_INFO_UPDATED_V1
import com.alexander.bryksin.kotlinspringcleanarchitecture.application.common.serializer.Serializer
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.models.Account
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.valueObjects.AccountId
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.valueObjects.PersonalInfo
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.outbox.models.OutboxEvent
import java.time.Instant
import java.util.*

data class PersonalInfoUpdatedEvent(
    val accountId: AccountId,
    val personalInfo: PersonalInfo = PersonalInfo(),
    val updatedAt: Instant? = null,
    val createdAt: Instant? = null,

    override val eventId: String,
    override val version: Long = 0,
    override val aggregateId: String,
    override val eventType: String,
    override val timestamp: Instant,
) : DomainEvent {
    companion object {
        const val ACCOUNT_PERSONAL_INFO_UPDATED_V1 = "ACCOUNT_PERSONAL_INFO_UPDATED_V1"
    }
}

fun Account.toPersonalInfoUpdatedEvent(): PersonalInfoUpdatedEvent {
    return PersonalInfoUpdatedEvent(
        accountId = accountId,
        aggregateId = accountId.id.toString(),
        eventId = UUID.randomUUID().toString(),
        eventType = ACCOUNT_PERSONAL_INFO_UPDATED_V1,
        timestamp = Instant.now(),
        personalInfo = personalInfo,
        version = version,
        updatedAt = updatedAt,
        createdAt = createdAt,
    )
}


fun Account.toPersonalInfoUpdatedOutboxEvent(serializer: Serializer): OutboxEvent {
    return this.toPersonalInfoUpdatedEvent().toOutboxEvent(serializer)
}


fun PersonalInfoUpdatedEvent.toOutboxEvent(serializer: Serializer) = OutboxEvent(
    eventId = UUID.randomUUID(),
    eventType = ACCOUNT_PERSONAL_INFO_UPDATED_V1,
    aggregateId = aggregateId,
    version = version,
    timestamp = Instant.now(),
    data = serializer.serializeToBytes(this)
)