package com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.valueObjects

data class Balance(val amount: Long = 0, val balanceCurrency: BalanceCurrency = BalanceCurrency.USD)
