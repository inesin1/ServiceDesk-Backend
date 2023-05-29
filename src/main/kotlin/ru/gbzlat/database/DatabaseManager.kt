package ru.gbzlat.database

import io.ktor.server.application.*
import org.ktorm.database.Database
import ru.gbzlat.database

fun Application.configureDatabase(env: ApplicationEnvironment) {
    database = DatabaseManager(
        hostname = env.config.propertyOrNull("database.hostname")?.getString() ?: "127.0.0.1",
        port = env.config.propertyOrNull("database.port")?.getString() ?: "3306",
        db = env.config.propertyOrNull("database.name")?.getString() ?: "sd_db",
        username = env.config.propertyOrNull("database.username")?.getString() ?: "root",
        password = env.config.propertyOrNull("database.password")?.getString() ?: "",
    )
}

class DatabaseManager(hostname: String, port: String, db: String, username: String, password: String) {
    val database: Database

    init {
        val jdbcUrl = "jdbc:mysql://$hostname:$port/$db?user=$username&password=$password&allowPublicKeyRetrieval=true&useSSL=false"
        database = Database.connect(jdbcUrl)
    }
}