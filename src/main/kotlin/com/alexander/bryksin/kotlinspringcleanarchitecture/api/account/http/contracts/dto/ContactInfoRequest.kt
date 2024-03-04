package com.alexander.bryksin.kotlinspringcleanarchitecture.api.account.http.contracts.dto

import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.valueObjects.ContactInfo
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.Size

data class ContactInfoRequest(
    @field:Size(min = 6, max = 250) @field:Email() val email: String,
    @field:Size(min = 6, max = 20) val phone: String
) {
    companion object
}

fun ContactInfoRequest.toContactInfo() = ContactInfo(
    email = email,
    phone = phone
)


