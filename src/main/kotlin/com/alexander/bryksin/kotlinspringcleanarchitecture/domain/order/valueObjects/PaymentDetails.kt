package com.alexander.bryksin.kotlinspringcleanarchitecture.domain.order.valueObjects

import java.time.OffsetDateTime

data class PaymentDetails(
    val cardNumber: String? = null,
    val transactionId: String? = null,
    val timestamp: OffsetDateTime? = null
)
