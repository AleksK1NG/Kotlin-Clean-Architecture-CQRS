package com.alexander.bryksin.kotlinspringcleanarchitecture.api.account.contracts.dto

import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.valueObjects.PersonalInfo
import jakarta.validation.constraints.Size

data class PersonalInfoRequest(
    @field:Size(max = 10_000) val bio: String = "",
    @field:Size(max = 500) val imageUrl: String = ""
) {
    companion object
}


fun PersonalInfoRequest.toPersonalInfo() = PersonalInfo(
    bio = bio,
    imageUrl = imageUrl
)