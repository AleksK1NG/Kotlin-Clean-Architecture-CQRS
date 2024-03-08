package com.alexander.bryksin.kotlinspringcleanarchitecture.api.account.http.contracts

import org.springframework.http.HttpStatus

data class BaseResponse<T : Any?>(
    val status: HttpStatus = HttpStatus.OK,
    val message: String = status.toString(),
    val data: T? = null
) {
    companion object
}
