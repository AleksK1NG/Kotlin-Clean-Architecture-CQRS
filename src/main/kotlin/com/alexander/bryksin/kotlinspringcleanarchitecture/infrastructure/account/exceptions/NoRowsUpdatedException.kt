package com.alexander.bryksin.kotlinspringcleanarchitecture.infrastructure.account.exceptions

data class NoRowsUpdatedException(val id: Any?, val version: Long?) :
    RuntimeException("no rows updated for id: $id and version: $version")
