package com.alexander.bryksin.kotlinspringcleanarchitecture.infrastructure.exceptions

data class OptimisticLockException(val id: Any?, val version: Long?) :
    RuntimeException("error while optimistic lock update id: $id version: $version")
