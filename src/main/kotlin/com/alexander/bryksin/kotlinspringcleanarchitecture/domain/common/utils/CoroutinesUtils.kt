package com.alexander.bryksin.kotlinspringcleanarchitecture.domain.common.utils

import kotlinx.coroutines.CancellationException


suspend inline fun <R> runSuspendCatching(block: () -> R): Result<R> {
    return try {
        Result.success(block())
    } catch (c: CancellationException) {
        throw c
    } catch (e: Throwable) {
        Result.failure(e)
    }
}