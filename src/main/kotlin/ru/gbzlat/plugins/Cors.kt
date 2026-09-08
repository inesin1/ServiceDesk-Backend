package ru.gbzlat.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.cors.routing.*

fun Application.configureCors(allowedHosts: List<String>) {
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
