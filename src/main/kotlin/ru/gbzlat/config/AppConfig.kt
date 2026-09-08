package ru.gbzlat.config

import io.ktor.server.config.*

class AppConfig(config: ApplicationConfig) {
    val jwt = Jwt(config.config("jwt"))
    val database = Database(config.config("database"))
    val corsAllowedHosts = config.property("cors.allowedHosts").getString()
        .split(",")
        .map { it.trim() }
        .filter { it.isNotEmpty() }
    val telegramToken = config.propertyOrNull("tgbot.token")?.getString()?.takeIf { it.isNotBlank() }

    class Jwt(config: ApplicationConfig) {
        val secret = config.property("secret").getString()
        val issuer = config.property("issuer").getString()
        val audience = config.property("audience").getString()
        val ttlHours = config.property("ttlHours").getString().toLong()
    }

    class Database(config: ApplicationConfig) {
        val hostname = config.property("hostname").getString()
        val port = config.property("port").getString()
        val name = config.property("name").getString()
        val username = config.property("username").getString()
        val password = config.property("password").getString()
        val poolSize = config.propertyOrNull("poolSize")?.getString()?.toInt() ?: 10
    }
}
