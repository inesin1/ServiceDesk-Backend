package ru.gbzlat.plugins

import io.ktor.server.application.*
import ru.gbzlat.database
import ru.gbzlat.database.DatabaseFactory

fun Application.configureDatabase(env: ApplicationEnvironment) {
    database = DatabaseFactory.createConnection(
        hostname = env.config.propertyOrNull("database.hostname")?.getString() ?: "127.0.0.1",
        port = env.config.propertyOrNull("database.port")?.getString() ?: "3306",
        db = env.config.propertyOrNull("database.name")?.getString() ?: "sd_db",
        username = env.config.propertyOrNull("database.username")?.getString() ?: "root",
        password = env.config.propertyOrNull("database.password")?.getString() ?: "",
    )
}
