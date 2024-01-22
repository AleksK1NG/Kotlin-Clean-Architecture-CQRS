package com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.commands

data class CreateAccountCommand(
    val email: String,
    val phone: String,
    val country: String,
    val city: String,
    val postCode: String,
) {
    companion object {}
}

