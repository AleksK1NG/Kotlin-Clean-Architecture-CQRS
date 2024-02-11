package com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.dto

import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.models.Account
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.common.outbox.models.OutboxEvent

data class AccountWithEvent(val account: Account, val event: OutboxEvent)
