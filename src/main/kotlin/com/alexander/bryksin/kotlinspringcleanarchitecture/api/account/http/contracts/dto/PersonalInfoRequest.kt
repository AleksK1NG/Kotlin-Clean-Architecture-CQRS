package com.alexander.bryksin.kotlinspringcleanarchitecture.api.account.http.contracts.dto

import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.valueObjects.PersonalInfo
import jakarta.validation.constraints.Size

data class PersonalInfoRequest(
    @field:Size(max = 10_000, min = 6) val bio: String,
    @field:Size(max = 500, min = 6) val imageUrl: String
) {
    companion object
}


fun PersonalInfoRequest.toPersonalInfo() = PersonalInfo(
    bio = bio,
    imageUrl = imageUrl
)