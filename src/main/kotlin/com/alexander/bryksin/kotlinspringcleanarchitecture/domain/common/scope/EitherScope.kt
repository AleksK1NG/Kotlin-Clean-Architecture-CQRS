package com.alexander.bryksin.kotlinspringcleanarchitecture.domain.common.scope

import arrow.core.raise.Raise
import arrow.core.raise.either
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.plus
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext


private val scope = CoroutineScope(Job() + Dispatchers.IO)

suspend fun <T> serviceScope(
    context: CoroutineContext = EmptyCoroutineContext,
    block: suspend CoroutineScope.() -> T
): T = block(scope + context)

suspend fun <L, R> eitherScope(
    context: CoroutineContext = EmptyCoroutineContext,
    block: suspend EitherContext<L>.() -> R
) = either {
    serviceScope {
        block(EitherCoroutine<L>(raiseErr = this@either, scope = this@serviceScope + context))
    }
}

interface EitherContext<L> : Raise<L>, CoroutineScope

class EitherCoroutine<L>(val raiseErr: Raise<L>, val scope: CoroutineScope) :
    Raise<L> by raiseErr,
    CoroutineScope by scope,
    EitherContext<L>