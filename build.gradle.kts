plugins {
	java
	id("org.springframework.boot") version "4.0.2"
	id("io.spring.dependency-management") version "1.1.7"
}

group = "com.chatapp"
version = "0.0.1-SNAPSHOT"
description = "Demo project for Spring Boot"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(25)
	}
}

configurations {
	compileOnly {
		extendsFrom(configurations.annotationProcessor.get())
	}
}

repositories {
	mavenCentral()
}

dependencies {

	// log
	implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.21.0")
	implementation("org.springframework.boot:spring-boot-starter-zipkin")
	implementation("org.springframework.boot:spring-boot-starter-actuator")

	// Database
	implementation("org.springframework.boot:spring-boot-starter-data-redis-reactive")
	implementation("org.springframework.boot:spring-boot-starter-data-cassandra-reactive")

	// WebFlux and WebSocket
	implementation("org.springframework.boot:spring-boot-starter-webflux")
	implementation("org.springframework.boot:spring-boot-starter-websocket")

	// lombok
	compileOnly("org.projectlombok:lombok")
	annotationProcessor("org.projectlombok:lombok")

	// Tests implementation
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("io.projectreactor:reactor-test")

	// Testcontainers (with versions)
	implementation("org.springframework.boot:spring-boot-test-autoconfigure:4.1.0-M2")
	testImplementation("org.testcontainers:testcontainers:1.20.3")
	testImplementation("org.testcontainers:junit-jupiter:1.20.3")
	testImplementation("org.testcontainers:cassandra:1.21.4")

	// Other test dependencies
	testImplementation("org.springframework.boot:spring-boot-micrometer-tracing-test")
	testImplementation("org.springframework.boot:spring-boot-starter-data-cassandra-reactive-test")
	testImplementation("org.springframework.boot:spring-boot-starter-flyway-test")
	testImplementation("org.springframework.boot:spring-boot-starter-websocket-test")
	testImplementation("org.springframework.boot:spring-boot-starter-zipkin-test")

	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
	testImplementation("com.redis:testcontainers-redis:2.2.4")
	testImplementation("org.testcontainers:testcontainers:2.0.3")

}

tasks.withType<Test> {
	useJUnitPlatform()
}
