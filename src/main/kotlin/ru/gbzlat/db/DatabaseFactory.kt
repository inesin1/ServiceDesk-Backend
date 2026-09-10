package ru.gbzlat.db

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.application.*
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.v1.jdbc.Database
import ru.gbzlat.config.AppConfig

fun Application.connectDatabase(config: AppConfig.Database): Database {
    val dataSource = HikariDataSource(HikariConfig().apply {
        jdbcUrl = "jdbc:postgresql://${config.hostname}:${config.port}/${config.name}"
        username = config.username
        password = config.password
        maximumPoolSize = config.poolSize
    })
    monitor.subscribe(ApplicationStopped) { dataSource.close() }

    Flyway.configure().dataSource(dataSource).load().migrate()

    return Database.connect(dataSource)
}
