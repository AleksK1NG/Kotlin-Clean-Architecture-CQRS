package com.alexander.bryksin.kotlinspringcleanarchitecture.infrastructure.configuration.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "mongo")
data class MongoProperties(val mongoURI: String)
