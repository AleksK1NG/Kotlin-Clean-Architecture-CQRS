package com.alexander.bryksin.kotlinspringcleanarchitecture.application.account.exceptions

data class UpperEventVersionException(val id: Any?, val expectedVersion: Any, val eventVersion: Any) : RuntimeException(
    "invalid version id: $id, expected version: $expectedVersion, eventVersion: $eventVersion"
) {
    companion object
}
