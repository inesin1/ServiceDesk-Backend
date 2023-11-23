package ru.gbzlat.plugins

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.routing.*
import ru.gbzlat.routes.*

fun Application.configureRouting() {
    routing {
        route("/api") {
            authRoute()
            authenticate ("auth-jwt") {
                userRoute()
                ticketRoute()
                departmentRoute()
            }
        }
    }
}