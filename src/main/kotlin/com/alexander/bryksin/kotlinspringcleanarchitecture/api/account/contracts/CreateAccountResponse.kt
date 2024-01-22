package com.alexander.bryksin.kotlinspringcleanarchitecture.api.account.contracts

import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.valueObjects.*

data class CreateAccountResponse(
    val accountId: AccountId,
    val contactInfo: ContactInfo = ContactInfo(),
    val address: Address = Address(),
    val balance: Balance = Balance(),
    val status: AccountStatus = AccountStatus.FREE,
) {
    companion object {}
}
