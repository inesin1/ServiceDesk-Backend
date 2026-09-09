package ru.gbzlat.plugins

import io.github.smiley4.ktoropenapi.openApi
import io.github.smiley4.ktorswaggerui.swaggerUI
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.routing.*
import ru.gbzlat.routes.*

fun Application.configureRouting() {
    routing {
        route("api.json") { openApi() }
        route("swagger") { swaggerUI("/api.json") }

        route("/api") {
            authRoute()
            authenticate("auth-jwt") {
                userRoute()
                ticketRoute()
                departmentRoute()
            }
        }
    }
}
