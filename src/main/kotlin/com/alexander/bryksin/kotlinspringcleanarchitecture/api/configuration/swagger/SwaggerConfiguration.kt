package com.alexander.bryksin.kotlinspringcleanarchitecture.api.configuration.swagger

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.servers.Server
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SwaggerConfiguration {

    @Bean
    fun openApi(): OpenAPI {
        return OpenAPI()
            .info(
                Info()
                    .title("Kotlin Spring Clean Architecture Microservice")
                    .description(
                        """
                            Clean Architecture Microservice
                        """.trimIndent(),
                    )
                    .contact(
                        Contact()
                            .name("Alexander Bryksin")
                            .email("alexander.bryksin@yandex.ru")
                            .url("https://github.com/AleksK1NG")
                    )
                    .version("1.0.0")
            )
            .addServersItem(Server().url("http://localhost:8080/").description("dev"))
    }
}