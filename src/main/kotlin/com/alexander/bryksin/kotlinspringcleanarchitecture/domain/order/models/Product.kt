package com.alexander.bryksin.kotlinspringcleanarchitecture.domain.order.models

import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.order.valueObjects.OrderId
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.order.valueObjects.Price
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.order.valueObjects.ProductId
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.order.valueObjects.ProductInfo
import java.time.OffsetDateTime

data class Product(
    val productId: ProductId? = null,
    val orderId: OrderId? = null,
    val productInfo: ProductInfo = ProductInfo(),
    val price: Price = Price(),
    val quantity: Long = 0,

    val version: Long = 0,
    val createdAt: OffsetDateTime? = null,
    val updatedAt: OffsetDateTime? = null,
) {

    fun incQuantity(amount: Long): Product = this.copy(quantity = this.quantity + amount)
    fun decQuantity(amount: Long): Product {
        if ((this.quantity - amount) < 0) throw RuntimeException("invalid amount")
        return this.copy(quantity = this.quantity - amount)
    }

    fun totalSum() = price.amount * quantity

    companion object {}
}
