package com.alexander.bryksin.kotlinspringcleanarchitecture.api.account.contracts.dto

import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.valueObjects.Address
import jakarta.validation.constraints.Size

data class AddressRequest(
    @field:Size(min = 6, max = 60) val country: String? = null,
    @field:Size(min = 6, max = 60) val city: String? = null,
    @field:Size(max = 20) val postCode: String? = null
) {
    companion object
}

fun AddressRequest.toAddress() = Address(
    country = country,
    city = city,
    postCode = postCode
)