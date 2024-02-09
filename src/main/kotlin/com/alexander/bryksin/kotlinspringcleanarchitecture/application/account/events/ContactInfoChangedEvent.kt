package com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.events

import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.valueObjects.AccountId
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.valueObjects.ContactInfo
import java.time.OffsetDateTime

data class ContactInfoChangedEvent(
    val accountId: AccountId,
    val contactInfo: ContactInfo = ContactInfo(),
    val version: Long = 0,
    val updatedAt: OffsetDateTime? = null,
    val createdAt: OffsetDateTime? = null,
) {
    companion object {}
}
