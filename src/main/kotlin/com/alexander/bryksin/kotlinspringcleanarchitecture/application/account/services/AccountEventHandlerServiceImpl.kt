package com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.services

import arrow.core.Either
import com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.events.*
import com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.persistance.AccountProjectionRepository
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.errors.AppError
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.errors.LowerEventVersionError
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.errors.SameEventVersionError
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.errors.UpperEventVersionError
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.models.Account
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.valueObjects.AccountId
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.common.scope.eitherScope
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import org.springframework.stereotype.Component


@Component
class AccountEventHandlerServiceImpl(
    private val accountProjectionRepository: AccountProjectionRepository
) : AccountEventHandlerService {

    override suspend fun on(event: AccountCreatedEvent): Either<AppError, Unit> = eitherScope(ctx) {
        accountProjectionRepository.save(event.toAccount()).bind()
    }

    override suspend fun on(event: BalanceDepositedEvent): Either<AppError, Unit> = eitherScope(ctx) {
        findAndUpdateAccountById(event.accountId, event.version) { foundAccount ->
            foundAccount.depositBalance(event.balance).bind()
        }.bind()
    }


    override suspend fun on(event: BalanceWithdrawEvent): Either<AppError, Unit> = eitherScope(ctx) {
        findAndUpdateAccountById(event.accountId, event.version) { foundAccount ->
            foundAccount.withdrawBalance(event.balance).bind()
        }.bind()
    }

    override suspend fun on(event: PersonalInfoUpdatedEvent): Either<AppError, Unit> = eitherScope(ctx) {
        findAndUpdateAccountById(event.accountId, event.version) { foundAccount ->
            foundAccount.changePersonalInfo(event.personalInfo).bind()
        }.bind()
    }


    override suspend fun on(event: ContactInfoChangedEvent): Either<AppError, Unit> = eitherScope(ctx) {
        findAndUpdateAccountById(event.accountId, event.version) { foundAccount ->
            foundAccount.changeContactInfo(event.contactInfo).bind()
        }.bind()
    }

    override suspend fun on(event: AccountStatusChangedEvent): Either<AppError, Unit> = eitherScope(ctx) {
        findAndUpdateAccountById(event.accountId, event.version) { foundAccount ->
            foundAccount.updateStatus(event.status).bind()
        }.bind()
    }

    private suspend fun findAndUpdateAccountById(
        accountId: AccountId,
        eventVersion: Long,
        block: suspend (Account) -> Account
    ): Either<AppError, Account> = eitherScope(ctx) {
        val foundAccount = findAndValidateVersion(accountId, eventVersion).bind()
        val accountToUpdate = block(foundAccount)
        accountProjectionRepository.update(accountToUpdate).bind()
    }
        .onRight { log.info { "mongo repository updated account: $it" } }
        .onLeft { log.error { "error while updating account: $it" } }

    private suspend fun findAndValidateVersion(
        accountId: AccountId,
        eventVersion: Long
    ): Either<AppError, Account> = eitherScope(ctx) {
        val foundAccount = accountProjectionRepository.getById(accountId).bind()
        validateVersion(foundAccount, eventVersion).bind()
        foundAccount
    }

    private val ctx = Job() + CoroutineName(this::class.java.name) + Dispatchers.IO

    private companion object {
        private val log = KotlinLogging.logger { }
    }
}


internal suspend fun validateVersion(account: Account, eventVersion: Long) = eitherScope<AppError, Unit> {
    when {
        eventVersion < account.version + 1 ->
            raise(LowerEventVersionError(account.accountId, account.version, eventVersion))

        eventVersion == account.version ->
            raise(SameEventVersionError(account.accountId, account.version, eventVersion))

        eventVersion > account.version + 1 ->
            raise(UpperEventVersionError(account.accountId, account.version, eventVersion))
    }
}

