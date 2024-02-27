package com.alexander.bryksin.kotlinspringcleanarchitecture.api.account.http

import com.alexander.bryksin.kotlinspringcleanarchitecture.api.account.contracts.*
import com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.queries.GetAccountByEmailQuery
import com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.queries.GetAccountByIdQuery
import com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.queries.GetAllAccountsQuery
import com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.services.AccountCommandService
import com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.services.AccountQueryService
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.valueObjects.AccountId
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.common.scope.eitherScope
import io.github.oshai.kotlinlogging.KotlinLogging
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import kotlinx.coroutines.CoroutineName
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

    @Operation(
        method = "createAccount", operationId = "createAccount", description = "Create new Account",
        responses = [
            ApiResponse(
                responseCode = "201",
                content = [Content(
                    schema = Schema(
                        implementation = AccountId::class
                    )
                )]
            )],
    )
    @PostMapping
    suspend fun createAccount(@Valid @RequestBody request: CreateAccountRequest): ResponseEntity<out Any> =
        eitherScope(ctx) {
            accountCommandService.handle(request.toCommand()).bind()
        }.fold(
            ifLeft = { mapErrorToResponse(it) },
            ifRight = { createdResponse(it) }
        )

    @Operation(
        method = "getAccountById", operationId = "getAccountById", description = "Get account by id",
        responses = [
            ApiResponse(
                responseCode = "200",
                content = [Content(
                    schema = Schema(
                        implementation = AccountResponse::class
                    )
                )]
            )],
    )
    @GetMapping(path = ["{id}"])
    suspend fun getAccountById(@PathVariable id: UUID): ResponseEntity<out Any> = eitherScope(ctx) {
        accountQueryService.handle(GetAccountByIdQuery(AccountId(id))).bind()
    }.fold(
        ifLeft = { mapErrorToResponse(it) },
        ifRight = { okResponse(it.toResponse()) }
    )

    @Operation(
        method = "depositBalance", operationId = "depositBalance", description = "Deposit balance",
        responses = [
            ApiResponse(
                responseCode = "200",
                content = [Content(
                    schema = Schema(
                        implementation = BaseResponse::class
                    )
                )]
            )],
    )
    @PutMapping(path = ["/{id}/deposit"])
    suspend fun depositBalance(
        @PathVariable id: UUID,
        @Valid @RequestBody request: DepositBalanceRequest
    ): ResponseEntity<out Any> = eitherScope(ctx) {
        accountCommandService.handle(request.toCommand(AccountId(id))).bind()
    }.fold(
        ifLeft = { mapErrorToResponse(it) },
        ifRight = { OK_RESPONSE }
    )

    @Operation(
        method = "withdrawBalance", operationId = "withdrawBalance", description = "Withdraw balance",
        responses = [
            ApiResponse(
                responseCode = "200",
                content = [Content(
                    schema = Schema(
                        implementation = BaseResponse::class
                    )
                )]
            )],
    )
    @PutMapping(path = ["/{id}/withdraw"])
    suspend fun withdrawBalance(
        @PathVariable id: UUID,
        @Valid @RequestBody request: WithdrawBalanceRequest
    ): ResponseEntity<out Any> = eitherScope(ctx) {
        accountCommandService.handle(request.toCommand(AccountId(id))).bind()
    }.fold(
        ifLeft = { mapErrorToResponse(it) },
        ifRight = { OK_RESPONSE }
    )

    @Operation(
        method = "updateStatus", operationId = "updateStatus", description = "Update account status",
        responses = [
            ApiResponse(
                responseCode = "200",
                content = [Content(
                    schema = Schema(
                        implementation = BaseResponse::class
                    )
                )]
            )],
    )
    @PutMapping(path = ["/{id}/status"])
    suspend fun updateStatus(
        @PathVariable id: UUID,
        @Valid @RequestBody request: ChangeAccountStatusRequest
    ): ResponseEntity<out Any> = eitherScope(ctx) {
        accountCommandService.handle(request.toCommand(AccountId(id))).bind()
    }.fold(
        ifLeft = { mapErrorToResponse(it) },
        ifRight = { OK_RESPONSE }
    )

    @Operation(
        method = "updatePersonalInfo", operationId = "updatePersonalInfo", description = "Update account info",
        responses = [
            ApiResponse(
                responseCode = "200",
                content = [Content(
                    schema = Schema(
                        implementation = BaseResponse::class
                    )
                )]
            )],
    )
    @PutMapping(path = ["/{id}/info/"])
    suspend fun updatePersonalInfo(
        @PathVariable id: UUID,
        @Valid @RequestBody request: UpdatePersonalInfoRequest
    ): ResponseEntity<out Any> = eitherScope(ctx) {
        accountCommandService.handle(request.toCommand(AccountId(id))).bind()
    }.fold(
        ifLeft = { mapErrorToResponse(it) },
        ifRight = { OK_RESPONSE }
    )

    @Operation(
        method = "changeContactInfo", operationId = "changeContactInfo", description = "Update account contacts",
        responses = [
            ApiResponse(
                responseCode = "200",
                content = [Content(
                    schema = Schema(
                        implementation = BaseResponse::class
                    )
                )]
            )],
    )
    @PutMapping(path = ["/{id}/contacts/"])
    suspend fun changeContactInfo(
        @PathVariable id: UUID,
        @Valid @RequestBody request: ChangeContactInfoRequest
    ): ResponseEntity<out Any> = eitherScope(ctx) {
        accountCommandService.handle(request.toCommand(AccountId(id))).bind()
    }.fold(
        ifLeft = { mapErrorToResponse(it) },
        ifRight = { OK_RESPONSE }
    )

    @Operation(
        method = "getAccountByEmail", operationId = "getAccountByEmail", description = "Get account by email",
        responses = [
            ApiResponse(
                responseCode = "200",
                content = [Content(
                    schema = Schema(
                        implementation = AccountResponse::class
                    )
                )]
            )],
    )
    @GetMapping(path = ["/email/{email}"])
    suspend fun getAccountByEmail(@PathVariable email: String): ResponseEntity<out Any> = eitherScope(ctx) {
        accountQueryService.handle(GetAccountByEmailQuery(email)).bind()
    }.fold(
        ifLeft = { mapErrorToResponse(it) },
        ifRight = { okResponse(it.toResponse()) }
    )

    @Operation(
        method = "getAllAccounts", operationId = "getAllAccounts", description = "Get all accounts",
        responses = [
            ApiResponse(
                responseCode = "200",
                content = [Content(
                    schema = Schema(
                        implementation = AccountsListResponse::class
                    )
                )]
            )],
    )
    @GetMapping(path = ["/all"])
    suspend fun getAllAccounts(
        @RequestParam(name = "page", required = false, defaultValue = "0") page: Int,
        @RequestParam(name = "size", required = false, defaultValue = "10") size: Int
    ): ResponseEntity<out Any> = eitherScope(ctx) {
        accountQueryService.handle(GetAllAccountsQuery(page = page, size = size)).bind()
    }.fold(
        ifLeft = { mapErrorToResponse(it) },
        ifRight = { okResponse(it.toHttpResponse()) }
    )

    private val ctx = Job() + CoroutineName(this::class.java.name)

    private companion object {
        private val log = KotlinLogging.logger { }
        private val OK_RESPONSE = ResponseEntity.status(HttpStatus.OK).body(BaseResponse(status = HttpStatus.OK))
    }
}

internal fun <T : Any> createdResponse(data: T) =
    ResponseEntity.status(HttpStatus.CREATED).body(BaseResponse(status = HttpStatus.CREATED, data = data))

internal fun <T : Any> okResponse(data: T) =
    ResponseEntity.status(HttpStatus.OK).body(BaseResponse(status = HttpStatus.OK, data = data))
