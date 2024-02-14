package com.alexander.bryksin.kotlinspringcleanarchitecture.infrastructure.configuration.mongo

import com.alexander.bryksin.kotlinspringcleanarchitecture.infrastructure.account.persistance.entity.AccountDocument
import com.mongodb.kotlin.client.coroutine.MongoClient
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.annotation.PostConstruct
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Component


@Component
class MongoInitializer(private val mongoClient: MongoClient) {

    @PostConstruct
    fun init() = runBlocking {
        try {
            mongoClient.listDatabases().collect { dbs -> log.info { "db names: $dbs.toJson()" } }

            val db = mongoClient.getDatabase("accounts")
            val collection = db.getCollection<AccountDocument>("accounts")
            log.info { "accounts collection: ${collection}" }


//
//            val article = AccountDocument(ObjectId(), "NICE =D", 5555)
////            val insertOneResult = collection.insertOne(article)
////
//
//
//            collection.find().collect { articles ->
//                log.info("all articles: {}", articles)
//            }
//
//            collection.find(Filters.eq("_id", ObjectId("65bcc5e5297bc26a037dd756"))).collect { articles ->
//                log.info("articles: {}", articles)
//            }
//            val updateFilter = Filters.eq("_id", ObjectId("65bcc5e5297bc26a037dd756"))
//            val updateSet = Updates.set(ArticleDocument::data.name, 55557777)
//            val findOneAndUpdateResult = collection.findOneAndUpdate(updateFilter, updateSet)
//            log.info("findOneAndUpdateResult: {}", findOneAndUpdateResult)
        } catch (e: Exception) {
            log.error { "error while creating mongo client: ${e.message}" }
        }
    }

    private companion object {
        private val log = KotlinLogging.logger { }
    }
}