package ru.gbzlat

import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.jackson.*
import io.ktor.server.config.*
import io.ktor.server.testing.*
import org.testcontainers.containers.PostgreSQLContainer

/**
 * One Postgres for the whole suite. Flyway runs on first boot, so every test
 * starts from the seeded schema.
 */
val postgres = PostgreSQLContainer("postgres:18-alpine").apply {
    withDatabaseName("service_desk")
    start()
}

fun withApp(block: suspend ApplicationTestBuilder.(HttpClient) -> Unit) = testApplication {
    environment {
        config = MapApplicationConfig(
            "jwt.secret" to "test-secret",
            "jwt.issuer" to "service-desk",
            "jwt.audience" to "service-desk-client",
            "jwt.realm" to "test",
            "jwt.ttlHours" to "1",
            "cors.allowedHosts" to "http://localhost",
            "database.hostname" to postgres.host,
            "database.port" to postgres.firstMappedPort.toString(),
            "database.name" to postgres.databaseName,
            "database.username" to postgres.username,
            "database.password" to postgres.password,
            "database.poolSize" to "2",
        )
    }
    application { module() }

    val client = createClient {
        install(ContentNegotiation) { jackson() }
        expectSuccess = false
    }

    block(client)
}
