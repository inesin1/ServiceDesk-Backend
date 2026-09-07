package ru.gbzlat.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.cors.routing.*

fun Application.configureCors() {
    val allowedHosts = environment.config.property("cors.allowedHosts").getString()
        .split(",")
        .map { it.trim() }
        .filter { it.isNotEmpty() }

    install(CORS) {
        allowedHosts.forEach { host ->
            val (scheme, name) = host.split("://").let { it.first() to it.last() }
            allowHost(name, schemes = listOf(scheme))
        }
        allowCredentials = true
        allowHeader(HttpHeaders.Accept)
        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.ContentType)
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
    }
}
