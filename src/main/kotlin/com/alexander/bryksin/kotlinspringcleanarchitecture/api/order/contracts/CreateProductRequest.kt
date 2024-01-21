package com.alexander.bryksin.kotlinspringcleanarchitecture.api.order.contracts

import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.order.valueObjects.Price
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.order.valueObjects.ProductInfo

data class CreateProductRequest(
    val productInfo: ProductInfo = ProductInfo(),
    val price: Price = Price(),
    val quantity: Long = 0,
) {
    companion object {}
}
