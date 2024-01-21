package com.alexander.bryksin.kotlinspringcleanarchitecture.domain.order.valueObjects

import java.util.*

data class ProductId(val id: UUID) {
    fun string() = id.toString()
}
