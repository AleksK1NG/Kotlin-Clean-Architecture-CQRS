package com.alexander.bryksin.kotlinspringcleanarchitecture.api.account.contracts

import com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.commands.CreateAccountCommand

data class CreateAccountRequest(
    val email: String,
    val phone: String,
    val country: String,
    val city: String,
    val postCode: String,
) {
    companion object {}
}

fun CreateAccountRequest.toCommand() = CreateAccountCommand(
    email = this.email,
    phone = this.phone,
    postCode = this.postCode,
    country = this.country,
    city = this.city,
)