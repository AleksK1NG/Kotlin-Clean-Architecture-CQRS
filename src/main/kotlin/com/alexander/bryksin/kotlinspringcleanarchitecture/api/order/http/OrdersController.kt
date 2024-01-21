package com.alexander.bryksin.kotlinspringcleanarchitecture.api.order.http

import com.alexander.bryksin.kotlinspringcleanarchitecture.api.order.contracts.CreateOrderRequest
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.coroutineScope
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping(path = ["/api/v1/orders"])
class OrdersController {

    @PostMapping
    suspend fun createOrder(@RequestBody request: CreateOrderRequest) = coroutineScope {
        log.info { "create order request: $request" }
        ResponseEntity.ok("NICE =D")
    }

    private companion object {
        private val log = KotlinLogging.logger { }
    }
}