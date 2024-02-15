package com.alexander.bryksin.kotlinspringcleanarchitecture.infrastructure.configuration.mongo

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
        try {
            mongoClient.listDatabases().collect { dbs -> log.info { "db names: ${dbs.toJson()}" } }

            val db = mongoClient.getDatabase("accounts")
            val collection = db.getCollection<AccountDocument>("accounts")
            log.info { "accounts collection: $collection" }

            val indexes = collection.listIndexes().map { it["name"] }.toSet()
            log.info { "indexes: $indexes" }

            if (!indexes.contains("email_1")) {
                val emailIndex = collection.createIndex(
                    keys = Indexes.ascending("email"),
                    options = IndexOptions(),
                )
                log.info { "created indexes: $emailIndex" }
            }

            if (!indexes.contains("accountId_1")) {
                val accountIdIndex = collection.createIndex(
                    keys = Indexes.ascending("accountId"),
                    options = IndexOptions(),
                )
                log.info { "created indexes: $accountIdIndex" }
            }


            val createdIndexes = collection.listIndexes().toSet()
            log.info { "createdIndexes: $createdIndexes" }
        } catch (e: Exception) {
            log.error { "error while creating mongo client: ${e.message}" }
        }
    }

    private companion object {
        private val log = KotlinLogging.logger { }
    }
}