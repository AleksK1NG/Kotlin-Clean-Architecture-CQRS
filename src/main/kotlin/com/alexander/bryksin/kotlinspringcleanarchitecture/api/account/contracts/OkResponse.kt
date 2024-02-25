package com.alexander.bryksin.kotlinspringcleanarchitecture.api.account.contracts

import org.springframework.http.HttpStatus

data class BaseResponse(
    val status: HttpStatus = HttpStatus.OK,
    val message: String = status.toString(),
    val data: Any? = null
) {
    companion object
}
