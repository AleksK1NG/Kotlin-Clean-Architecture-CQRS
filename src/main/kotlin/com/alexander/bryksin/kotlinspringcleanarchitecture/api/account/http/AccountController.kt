package com.alexander.bryksin.kotlinspringcleanarchitecture.api.account.http

import com.alexander.bryksin.kotlinspringcleanarchitecture.api.account.contracts.*
import com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.queries.GetAccountByIdQuery
import com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.services.AccountCommandService
import com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.services.AccountQueryService
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.valueObjects.AccountId
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.*
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext


@RestController
@RequestMapping(path = ["/api/v1/accounts"])
class AccountController(
    private val accountCommandService: AccountCommandService,
    private val accountQueryService: AccountQueryService
) {

    @PostMapping
    suspend fun createAccount(@RequestBody request: CreateAccountRequest) = controllerScope {
        accountCommandService.handle(request.toCommand())
            .let { ResponseEntity.status(HttpStatus.CREATED).body(it) }
    }

    @GetMapping(path = ["{id}"])
    suspend fun getAccountById(@PathVariable id: UUID) = controllerScope {
        accountQueryService.handle(GetAccountByIdQuery(id))
            .let { ResponseEntity.status(HttpStatus.OK).body(it) }
    }

    @PutMapping(path = ["/deposit/{id}"])
    suspend fun depositBalance(
        @PathVariable id: UUID,
        @RequestBody request: DepositBalanceRequest
    ) = controllerScope {
        val account = accountCommandService.handle(request.toCommand(AccountId(id)))
        ResponseEntity.ok(account)
    }

    @PutMapping(path = ["/withdraw/{id}"])
    suspend fun withdrawBalance(
        @PathVariable id: UUID,
        @RequestBody request: WithdrawBalanceRequest
    ) = controllerScope {
        val account = accountCommandService.handle(request.toCommand(AccountId(id)))
        ResponseEntity.ok(account)
    }

    @PutMapping(path = ["/status/{id}"])
    suspend fun updateStatus(
        @PathVariable id: UUID,
        @RequestBody request: ChangeAccountStatusRequest
    ) = controllerScope {
        val account = accountCommandService.handle(request.toCommand(AccountId(id)))
        ResponseEntity.ok(account)
    }


    private val scope = CoroutineScope(Job() + CoroutineName(this::class.java.name) + Dispatchers.IO)

    private suspend fun <T> controllerScope(
        context: CoroutineContext = EmptyCoroutineContext,
        block: suspend (CoroutineScope) -> T
    ): T = block(scope + context)

    private companion object {
        private val log = KotlinLogging.logger { }
    }
}