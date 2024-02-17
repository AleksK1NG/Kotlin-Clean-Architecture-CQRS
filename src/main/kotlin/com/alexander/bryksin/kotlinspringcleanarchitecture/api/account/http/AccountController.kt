package com.alexander.bryksin.kotlinspringcleanarchitecture.api.account.http

import com.alexander.bryksin.kotlinspringcleanarchitecture.api.account.contracts.*
import com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.queries.GetAccountByIdQuery
import com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.services.AccountCommandService
import com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.services.AccountQueryService
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.valueObjects.AccountId
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.validation.Valid
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
    suspend fun createAccount(@Valid @RequestBody request: CreateAccountRequest) = controllerScope {
        accountCommandService.handle(request.toCommand())
            .let { ResponseEntity.status(HttpStatus.CREATED).body(it) }
    }

    @GetMapping(path = ["{id}"])
    suspend fun getAccountById(@PathVariable id: UUID) = controllerScope {
        accountQueryService.handle(GetAccountByIdQuery(id))
            .let { ResponseEntity.status(HttpStatus.OK).body(it) }
    }

    @PutMapping(path = ["/{id}/deposit"])
    suspend fun depositBalance(
        @PathVariable id: UUID,
        @Valid @RequestBody request: DepositBalanceRequest
    ) = controllerScope {
        accountCommandService.handle(request.toCommand(AccountId(id)))
            .let { ResponseEntity.ok(it) }
    }

    @PutMapping(path = ["/{id}/withdraw"])
    suspend fun withdrawBalance(
        @PathVariable id: UUID,
        @Valid @RequestBody request: WithdrawBalanceRequest
    ) = controllerScope {
        accountCommandService.handle(request.toCommand(AccountId(id)))
            .let { ResponseEntity.ok(it) }
    }

    @PutMapping(path = ["/{id}/status"])
    suspend fun updateStatus(
        @PathVariable id: UUID,
        @Valid @RequestBody request: ChangeAccountStatusRequest
    ) = controllerScope {
        accountCommandService.handle(request.toCommand(AccountId(id)))
            .let { ResponseEntity.ok(it) }
    }

    @PutMapping(path = ["/{id}/info/"])
    suspend fun updatePersonalInfo(
        @PathVariable id: UUID,
        @Valid @RequestBody request: UpdatePersonalInfoRequest
    ) = controllerScope {
        accountCommandService.handle(request.toCommand(AccountId(id)))
            .let { ResponseEntity.ok(it) }
    }

    @PutMapping(path = ["/{id}/contacts/"])
    suspend fun changeContactInfo(
        @PathVariable id: UUID,
        @Valid @RequestBody request: ChangeAccountStatusRequest
    ) = controllerScope {
        accountCommandService.handle(request.toCommand(AccountId(id)))
            .let { ResponseEntity.ok(it) }
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