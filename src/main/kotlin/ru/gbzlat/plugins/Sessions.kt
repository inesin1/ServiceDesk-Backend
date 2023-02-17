package ru.gbzlat.plugins

import io.ktor.server.application.*
import io.ktor.server.sessions.*

data class AuthorizationHeader(val token: String)

fun Application.configureSessions() {
    install(Sessions) {
        cookie<AuthorizationHeader>("Authorization-Token") {
            cookie.maxAgeInSeconds = 1800
        }
    }
}