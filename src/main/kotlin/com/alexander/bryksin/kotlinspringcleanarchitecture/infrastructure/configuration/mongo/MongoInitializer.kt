package com.alexander.bryksin.kotlinspringcleanarchitecture.infrastructure.configuration.mongo

import com.alexander.bryksin.kotlinspringcleanarchitecture.domain.common.utils.runSuspendCatching
import com.alexander.bryksin.kotlinspringcleanarchitecture.infrastructure.account.persistance.entity.AccountDocument
import com.mongodb.client.model.IndexOptions
import com.mongodb.client.model.Indexes
import com.mongodb.kotlin.client.coroutine.MongoClient
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.annotation.PostConstruct
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toSet
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Component


@Component
class MongoInitializer(private val mongoClient: MongoClient) {

    @PostConstruct
    fun init() = runBlocking {
        runSuspendCatching {
            mongoClient.listDatabases().collect { dbs -> log.info { "db names: ${dbs.toJson()}" } }

            val db = mongoClient.getDatabase(ACCOUNTS_DB)
            val collection = db.getCollection<AccountDocument>(ACCOUNTS_COLLECTION)
            log.info { "accounts collection: $collection" }

            val indexes = collection.listIndexes().map { it["name"] }.toSet()
            log.info { "account collection indexes: $indexes" }

            if (!indexes.contains(EMAIL_INDEX)) {
                val emailIndex = collection.createIndex(
                    keys = Indexes.ascending(EMAIL),
                    options = IndexOptions(),
                )
                log.info { "mongo account email index: $emailIndex" }
            }

            if (!indexes.contains(ACCOUNT_ID_INDEX)) {
                val accountIdIndex = collection.createIndex(
                    keys = Indexes.ascending(ACCOUNT_ID),
                    options = IndexOptions(),
                )
                log.info { "account id index: $accountIdIndex" }
            }

            val createdIndexes = collection.listIndexes().toSet()
            log.info { "mongodb created indexes: $createdIndexes" }
        }
            .onSuccess { log.info { "mongo successfully initialized" } }
            .onFailure { log.error { "error while creating mongo client: ${it.message}" } }
    }

    private companion object {
        private val log = KotlinLogging.logger { }

        private const val ACCOUNTS_COLLECTION = "accounts"
        private const val ACCOUNTS_DB = "accounts"
        private const val EMAIL_INDEX = "email_1"
        private const val ACCOUNT_ID_INDEX = "accountId_1"
        private const val ACCOUNT_ID = "accountId"
        private const val EMAIL = "email"
    }
}