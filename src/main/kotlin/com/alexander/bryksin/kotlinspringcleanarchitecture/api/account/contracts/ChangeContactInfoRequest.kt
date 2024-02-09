package com.alexander.bryksin.kotlinspringcleanarchitecture.api.account.contracts

import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.valueObjects.ContactInfo

data class ChangeContactInfoRequest(val contactInfo: ContactInfo) {
    companion object {}
}
