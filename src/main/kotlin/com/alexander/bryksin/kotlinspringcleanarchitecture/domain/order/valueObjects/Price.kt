package com.alexander.bryksin.kotlinspringcleanarchitecture.domain.order.valueObjects

data class Price(val amount: Long = 0, val priceCurrency: PriceCurrency = PriceCurrency.USD)
