package ru.gbzlat.db

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.config.*
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.v1.jdbc.Database

fun connectDatabase(config: ApplicationConfig): Database {
    val host = config.property("database.hostname").getString()
    val port = config.property("database.port").getString()
    val name = config.property("database.name").getString()

    val dataSource = HikariDataSource(HikariConfig().apply {
        jdbcUrl = "jdbc:postgresql://$host:$port/$name"
        username = config.property("database.username").getString()
        password = config.property("database.password").getString()
        maximumPoolSize = config.propertyOrNull("database.poolSize")?.getString()?.toInt() ?: 10
    })

    Flyway.configure().dataSource(dataSource).load().migrate()

    return Database.connect(dataSource)
}
