package ru.gbzlat.db

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.v1.jdbc.Database
import ru.gbzlat.config.AppConfig

fun connectDatabase(config: AppConfig.Database): Database {
    val dataSource = HikariDataSource(HikariConfig().apply {
        jdbcUrl = "jdbc:postgresql://${config.hostname}:${config.port}/${config.name}"
        username = config.username
        password = config.password
        maximumPoolSize = config.poolSize
    })

    Flyway.configure().dataSource(dataSource).load().migrate()

    return Database.connect(dataSource)
}
