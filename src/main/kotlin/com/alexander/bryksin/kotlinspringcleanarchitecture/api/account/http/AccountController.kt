package com.alexander.bryksin.kotlinspringcleanarchitecture.api.account.http

import com.alexander.bryksin.kotlinspringcleanarchitecture.api.account.contracts.CreateAccountRequest
import com.alexander.bryksin.kotlinspringcleanarchitecture.api.account.contracts.toCommand
import com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.queries.GetAccountByIdQuery
import com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.services.AccountCommandService
import com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.services.AccountQueryService
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.*
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*
import kotlin.coroutines.CoroutineContext


@RestController
@RequestMapping(path = ["/api/v1/accounts"])
class AccountController(
    private val accountCommandService: AccountCommandService,
    private val accountQueryService: AccountQueryService
) {

    @PostMapping
    suspend fun createAccount(@RequestBody request: CreateAccountRequest) = coroutineScope {
        supervisorScope {

        }
        log.info { "POST create account request: $request" }
        val account = accountCommandService.handle(request.toCommand())
        ResponseEntity.ok(account)
    }

    @GetMapping(path = ["{id}"])
    suspend fun getAccountById(@PathVariable id: UUID) = coroutineScope {
        controllerScope {
            launch { log.info { "NICE =D" } }
        }
        val account = accountQueryService.handle(GetAccountByIdQuery(id))
        ResponseEntity.ok(account)
    }

    private companion object {
        private val log = KotlinLogging.logger { }
    }

    private val scope = CoroutineScope(Job() + Dispatchers.IO)
    private suspend fun <R> controllerScope(block: CoroutineScope.() -> R): R {
        return block(scope)
    }
}


class ApiScope(private val scope: CoroutineContext = SupervisorJob()) : CoroutineScope {
    override val coroutineContext: CoroutineContext = scope


}