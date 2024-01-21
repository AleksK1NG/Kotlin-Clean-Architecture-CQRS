package com.alexander.bryksin.kotlinspringcleanarchitecture.api.order.contracts

import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.order.valueObjects.Address
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.order.valueObjects.ContactInfo
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.order.valueObjects.PaymentDetails

data class CreateOrderRequest(
    val contactInfo: ContactInfo = ContactInfo(),
    val address: Address = Address(),
    val paymentDetails: PaymentDetails = PaymentDetails(),
    val products: Set<CreateProductRequest> = setOf()
) {
    companion object {}
}
