package com.alexander.bryksin.kotlinspringcleanarchitecture.infrastructure.account.persistance.repository

import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.models.Account
import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.account.valueObjects.*
import io.r2dbc.spi.Row
import java.math.BigInteger
import java.time.Instant
import java.util.*

internal fun Account.toPostgresEntityMap(withOptimisticLock: Boolean = false): MutableMap<String, *> {
    val map = mutableMapOf(
        "id" to accountId?.id,
        "email" to contactInfo.email,
        "phone" to contactInfo.phone,
        "country" to address.country,
        "city" to address.city,
        "post_code" to address.postCode,
        "bio" to personalInfo.bio,
        "image_url" to personalInfo.imageUrl,
        "balance_amount" to balance.amount,
        "balance_currency" to balance.balanceCurrency.name,
        "status" to status.name,
        "version" to version,
        "created_at" to createdAt,
        "updated_at" to updatedAt,
    )

    if (withOptimisticLock) {
        map["version"] = version + 1
        map["prev_version"] = version
    }

    return map
}

internal fun Row.toAccount(): Account {
    return Account(
        accountId = AccountId(get("id", UUID::class.java)!!),
        contactInfo = ContactInfo(
            email = get("email", String::class.java) ?: "",
            phone = get("phone", String::class.java) ?: "",
        ),
        personalInfo = PersonalInfo(
            bio = get("bio", String::class.java) ?: "",
            imageUrl = get("image_url", String::class.java) ?: "",
        ),
        address = Address(
            country = get("country", String::class.java) ?: "",
            city = get("city", String::class.java) ?: "",
            postCode = get("post_code", String::class.java) ?: "",
        ),
        balance = Balance(
            amount = get("balance_amount", BigInteger::class.java)?.toLong() ?: 0,
            balanceCurrency = BalanceCurrency.valueOf(get("balance_currency", String::class.java) ?: "")
        ),
        status = AccountStatus.valueOf(get("status", String::class.java) ?: ""),
        version = get("version", BigInteger::class.java)?.toLong() ?: 0,
        updatedAt = get("updated_at", Instant::class.java) ?: Instant.now(),
        createdAt = get("created_at", Instant::class.java) ?: Instant.now(),
    )
}