package com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.commands

import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.valueObjects.AccountId
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.valueObjects.Balance

data class WithdrawBalanceCommand(val accountId: AccountId, val balance: Balance, val transactionId: String) :
    AccountDomainCommand {
    companion object {}
}
