package com.alexander.bryksin.kotlinspringcleanarchitecture.api.account.contracts

import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.valueObjects.PersonalInfo

data class UpdatePersonalInfoRequest(val personalInfo: PersonalInfo) {
    companion object {}
}
