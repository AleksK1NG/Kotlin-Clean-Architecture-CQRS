package com.alexander.bryksin.kotlinspringcleanarchitecture.api.order.contracts

import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.valueObjects.Address
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.order.models.OrderStatus
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.order.models.Product
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.order.valueObjects.*
import java.time.OffsetDateTime

data class CreateOrderResponse(
    val orderId: OrderId? = null,
    val contactInfo: ContactInfo = ContactInfo(),
    val address: Address = Address(),
    val paymentDetails: PaymentDetails = PaymentDetails(),
    val status: OrderStatus = OrderStatus.NEW,

    val version: Long = 0,
    val updatedAt: OffsetDateTime? = null,
    val createdAt: OffsetDateTime? = null,

    val products: MutableMap<ProductId, Product> = mutableMapOf()
) {
}