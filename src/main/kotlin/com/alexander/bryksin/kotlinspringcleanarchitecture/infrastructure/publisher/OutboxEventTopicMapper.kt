package com.alexander.bryksin.kotlinspringcleanarchitecture.infrastructure.publisher

import com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.events.*
import com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.events.AccountCreatedEvent.Companion.ACCOUNT_CREATED_EVENT_V1
import com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.events.AccountStatusChangedEvent.Companion.ACCOUNT_STATUS_CHANGED_V1
import com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.events.BalanceDepositedEvent.Companion.ACCOUNT_BALANCE_DEPOSITED_V1
import com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.events.BalanceWithdrawEvent.Companion.ACCOUNT_BALANCE_WITHDRAW_V1
import com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.events.ContactInfoChangedEvent.Companion.ACCOUNT_CONTACT_INFO_CHANGED_V1
import com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.events.PersonalInfoUpdatedEvent.Companion.ACCOUNT_PERSONAL_INFO_UPDATED_V1
import com.alexander.bryksin.kotlinspringcleanarchitecture.application.common.serializer.Serializer
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.common.outbox.models.OutboxEvent

fun OutboxEvent.kafkaTopic() = when (eventType) {
    ACCOUNT_CREATED_EVENT_V1 -> "account-created-v1"
    ACCOUNT_STATUS_CHANGED_V1 -> "account-status-changed-v1"
    ACCOUNT_BALANCE_DEPOSITED_V1 -> "account-balance-deposited-v1"
    ACCOUNT_BALANCE_WITHDRAW_V1 -> "account-balance-withdraw-v1"
    ACCOUNT_CONTACT_INFO_CHANGED_V1 -> "account-contact-info-changed-v1"
    ACCOUNT_PERSONAL_INFO_UPDATED_V1 -> "account-info-updated-v1"
    else -> "dead-letter-queue"
}


fun accountEventFromTopic(topic: String, serializer: Serializer): Class<*> = when (topic) {
    ACCOUNT_CREATED_EVENT_V1 -> AccountCreatedEvent::class.java
    ACCOUNT_STATUS_CHANGED_V1 -> AccountStatusChangedEvent::class.java
    ACCOUNT_BALANCE_DEPOSITED_V1 -> BalanceDepositedEvent::class.java
    ACCOUNT_BALANCE_WITHDRAW_V1 -> BalanceWithdrawEvent::class.java
    ACCOUNT_CONTACT_INFO_CHANGED_V1 -> ContactInfoChangedEvent::class.java
    ACCOUNT_PERSONAL_INFO_UPDATED_V1 -> PersonalInfoUpdatedEvent::class.java
    else -> throw RuntimeException("Unknown topic: $topic")
}