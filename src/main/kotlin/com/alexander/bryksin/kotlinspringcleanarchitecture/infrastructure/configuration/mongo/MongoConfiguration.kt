package com.alexander.bryksin.kotlinspringcleanarchitecture.infrastructure.configuration.mongo

import com.mongodb.kotlin.client.coroutine.MongoClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class MongoConfiguration(@Value("\${mongo.mongoURI}") private val mongoURI: String) {

    @Bean
    fun mongoClient(): MongoClient = MongoClient.create(mongoURI)
}