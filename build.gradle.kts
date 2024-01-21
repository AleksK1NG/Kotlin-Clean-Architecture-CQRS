import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	id("org.springframework.boot") version "3.2.2"
	id("io.spring.dependency-management") version "1.1.4"
	kotlin("jvm") version "1.9.22"
	kotlin("plugin.spring") version "1.9.22"
}

group = "com.alexander.bryksin"
version = "0.0.1-SNAPSHOT"

java {
	sourceCompatibility = JavaVersion.VERSION_21
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("org.springframework.boot:spring-boot-starter-data-r2dbc")
	implementation("org.springframework.boot:spring-boot-starter-webflux")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
	implementation("org.flywaydb:flyway-core")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
	implementation("org.springframework:spring-jdbc")
	runtimeOnly("org.postgresql:postgresql")
	runtimeOnly("org.postgresql:r2dbc-postgresql")


	// Coroutines
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.7.3")

	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("io.github.oshai:kotlin-logging-jvm:6.0.1")

	implementation("io.netty:netty-all:4.1.106.Final")

	// Observation
	runtimeOnly("io.micrometer:micrometer-registry-otlp")
	runtimeOnly("io.micrometer:micrometer-tracing-bridge-otel")
	runtimeOnly("io.opentelemetry:opentelemetry-exporter-otlp")
	implementation("io.micrometer:context-propagation:1.1.0")
	implementation("io.projectreactor:reactor-core-micrometer:1.1.1")

	// AOP
	implementation("org.springframework.boot:spring-boot-starter-aop")

	// Swagger
	implementation("org.springdoc:springdoc-openapi-starter-webflux-ui:2.3.0")

	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("io.projectreactor:reactor-test")
}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs += "-Xjsr305=strict"
		jvmTarget = "21"
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}
