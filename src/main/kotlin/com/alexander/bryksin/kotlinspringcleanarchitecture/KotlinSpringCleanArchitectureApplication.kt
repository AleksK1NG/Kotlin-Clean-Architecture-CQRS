package com.alexander.bryksin.kotlinspringcleanarchitecture

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import org.springframework.data.r2dbc.config.EnableR2dbcAuditing
import org.springframework.scheduling.annotation.EnableScheduling
import reactor.core.publisher.Hooks

@SpringBootApplication
@EnableR2dbcAuditing
@EnableScheduling
@ConfigurationPropertiesScan
class KotlinSpringCleanArchitectureApplication

fun main(args: Array<String>) {
	runApplication<KotlinSpringCleanArchitectureApplication>(*args)
}
