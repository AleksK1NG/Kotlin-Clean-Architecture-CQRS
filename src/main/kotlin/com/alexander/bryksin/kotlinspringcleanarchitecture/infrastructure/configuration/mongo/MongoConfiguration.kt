package com.alexander.bryksin.kotlinspringcleanarchitecture.infrastructure.configuration.mongo

import com.mongodb.kotlin.client.coroutine.MongoClient
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class MongoConfiguration {

    @Bean
    fun mongoClient(): MongoClient = MongoClient.create("mongodb://mongo:mongo@localhost:27017/?maxPoolSize=20&w=majority")


    private companion object {
        private val log = KotlinLogging.logger { }
    }
}