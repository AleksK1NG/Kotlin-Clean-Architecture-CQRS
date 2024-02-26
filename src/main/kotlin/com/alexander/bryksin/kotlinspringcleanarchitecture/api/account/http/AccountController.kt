package com.alexander.bryksin.kotlinspringcleanarchitecture.api.account.http

import arrow.core.Either
import com.alexander.bryksin.kotlinspringcleanarchitecture.api.account.contracts.*
import com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.queries.GetAccountByEmailQuery
import com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.queries.GetAccountByIdQuery
import com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.queries.GetAllAccountsQuery
import com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.services.AccountCommandService
import com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.services.AccountQueryService
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.errors.AppError
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.valueObjects.AccountId
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.common.scope.eitherScope
import io.github.oshai.kotlinlogging.KotlinLogging
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*


@Tag(name = "Accounts", description = "Account domain REST endpoints")
@RestController
@RequestMapping(path = ["/api/v1/accounts"])
class AccountController(
    private val accountCommandService: AccountCommandService,
    private val accountQueryService: AccountQueryService
) {

    @Operation(method = "createAccount", operationId = "createAccount", description = "Create new Account")
    @PostMapping
    suspend fun createAccount(@Valid @RequestBody request: CreateAccountRequest): Either<AppError, ResponseEntity<*>> =
        eitherScope {
            accountCommandService.handle(request.toCommand()).fold(
                ifLeft = { mapErrorToResponse(it) },
                ifRight = { createdResponse(it) }
            )
        }

    @Operation(method = "getAccountById", operationId = "getAccountById", description = "Get account by id")
    @GetMapping(path = ["{id}"])
    suspend fun getAccountById(@PathVariable id: UUID): Either<AppError, ResponseEntity<*>> = eitherScope {
        accountQueryService.handle(GetAccountByIdQuery(AccountId(id))).fold(
            ifLeft = { mapErrorToResponse(it) },
            ifRight = { okResponse(it) }
        )
    }

    @Operation(method = "depositBalance", operationId = "depositBalance", description = "Deposit balance")
    @PutMapping(path = ["/{id}/deposit"])
    suspend fun depositBalance(
        @PathVariable id: UUID,
        @Valid @RequestBody request: DepositBalanceRequest
    ): Either<AppError, ResponseEntity<*>> = eitherScope {
        accountCommandService.handle(request.toCommand(AccountId(id))).fold(
            ifLeft = { mapErrorToResponse(it) },
            ifRight = { OK_RESPONSE }
        )
    }

    @Operation(method = "withdrawBalance", operationId = "withdrawBalance", description = "Withdraw balance")
    @PutMapping(path = ["/{id}/withdraw"])
    suspend fun withdrawBalance(
        @PathVariable id: UUID,
        @Valid @RequestBody request: WithdrawBalanceRequest
    ): Either<AppError, ResponseEntity<*>> = eitherScope {
        accountCommandService.handle(request.toCommand(AccountId(id))).fold(
            ifLeft = { mapErrorToResponse(it) },
            ifRight = { OK_RESPONSE }
        )
    }

    @Operation(method = "updateStatus", operationId = "updateStatus", description = "Update account status")
    @PutMapping(path = ["/{id}/status"])
    suspend fun updateStatus(
        @PathVariable id: UUID,
        @Valid @RequestBody request: ChangeAccountStatusRequest
    ): Either<AppError, ResponseEntity<*>> = eitherScope {
        accountCommandService.handle(request.toCommand(AccountId(id))).fold(
            ifLeft = { mapErrorToResponse(it) },
            ifRight = { OK_RESPONSE }
        )
    }

    @Operation(method = "updatePersonalInfo", operationId = "updatePersonalInfo", description = "Update account info")
    @PutMapping(path = ["/{id}/info/"])
    suspend fun updatePersonalInfo(
        @PathVariable id: UUID,
        @Valid @RequestBody request: UpdatePersonalInfoRequest
    ): Either<AppError, ResponseEntity<*>> = eitherScope {
        accountCommandService.handle(request.toCommand(AccountId(id))).fold(
            ifLeft = { mapErrorToResponse(it) },
            ifRight = { OK_RESPONSE }
        )
    }

    @Operation(method = "changeContactInfo", operationId = "changeContactInfo", description = "Update account contacts")
    @PutMapping(path = ["/{id}/contacts/"])
    suspend fun changeContactInfo(
        @PathVariable id: UUID,
        @Valid @RequestBody request: ChangeContactInfoRequest
    ): Either<AppError, ResponseEntity<*>> = eitherScope {
        accountCommandService.handle(request.toCommand(AccountId(id))).fold(
            ifLeft = { mapErrorToResponse(it) },
            ifRight = { OK_RESPONSE }
        )
    }

    @Operation(method = "getAccountByEmail", operationId = "getAccountByEmail", description = "Get account by email")
    @GetMapping(path = ["/email/{email}"])
    suspend fun getAccountByEmail(@PathVariable email: String): Either<AppError, ResponseEntity<*>> = eitherScope {
        accountQueryService.handle(GetAccountByEmailQuery(email)).fold(
            ifLeft = { mapErrorToResponse(it) },
            ifRight = { okResponse(it) }
        )
    }

    @Operation(method = "getAllAccounts", operationId = "getAccountByEmail", description = "Get all accounts")
    @GetMapping(path = ["/all"])
    suspend fun getAllAccounts(
        @RequestParam(name = "page", required = false, defaultValue = "0") page: Int,
        @RequestParam(name = "size", required = false, defaultValue = "10") size: Int
    ): Either<AppError, ResponseEntity<*>> = eitherScope {
        accountQueryService.handle(GetAllAccountsQuery(page = page, size = size)).fold(
            ifLeft = { mapErrorToResponse(it) },
            ifRight = { okResponse(it) }
        )
    }

    private val ctx = Job() + CoroutineName(this::class.java.name)
    private val scope = CoroutineScope(Job() + CoroutineName(this::class.java.name))


    private companion object {
        private val log = KotlinLogging.logger { }
        private val OK_RESPONSE = ResponseEntity.status(HttpStatus.OK).body(BaseResponse(status = HttpStatus.OK))
    }
}

fun <T : Any> createdResponse(data: T) =
    ResponseEntity.status(HttpStatus.CREATED).body(BaseResponse(status = HttpStatus.CREATED, data = data))

fun <T : Any> okResponse(data: T) =
    ResponseEntity.status(HttpStatus.OK).body(BaseResponse(status = HttpStatus.OK, data = data))
