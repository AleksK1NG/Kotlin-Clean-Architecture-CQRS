package com.alexander.bryksin.kotlinspringcleanarchitecture.api.filters

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono

@Component
@Order(1)
class LoggingFilter : WebFilter {

    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        val startTime = System.currentTimeMillis()

        return chain.filter(exchange)
            .doOnSuccess {
                if (!exchange.request.path.value().contains("actuator")) {
                    val method = exchange.request.method.name().uppercase()
                    val path = exchange.request.path.value()
                    val statusCode = exchange.response.statusCode?.value()
                    val time = (System.currentTimeMillis() - startTime)
                    log.info { "$method $path $statusCode ${time}ms" }
                }
            }
    }

    private companion object {
        private val log = KotlinLogging.logger { }
    }
}