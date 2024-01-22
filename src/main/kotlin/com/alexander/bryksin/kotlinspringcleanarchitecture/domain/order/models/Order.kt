package com.alexander.bryksin.kotlinspringcleanarchitecture.domain.order.models

import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.valueObjects.Address
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.order.valueObjects.ContactInfo
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.order.valueObjects.OrderId
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.order.valueObjects.PaymentDetails
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.order.valueObjects.ProductId
import java.time.OffsetDateTime

data class Order(
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
    companion object {}

    fun addProduct(product: Product) {
        if (products.containsKey(product.productId)) {
            val newProduct = products[product.productId]?.incQuantity(product.quantity)
            products[newProduct?.productId!!] = newProduct
            return
        }
        products[product.productId!!] = product
    }

    fun removeProduct(productId: ProductId) = products.remove(productId)

    fun totalSum() = products.map { it.value.totalSum() }.sum()
}