package ru.gbzlat.plugins

import io.ktor.server.routing.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.request.*
import ru.gbzlat.models.Auth

fun Application.configureRouting() {
    routing {
        get("/") {
            call.respond("sheeesh")
        }
        post ("/api/auth") {
            val auth = call.receive<Auth>()
            if (auth.login == "nesinai" && auth.password == "123")
                call.respond(" {\"message\": \"You're successfully authenticated!\"} ")
            else
                call.respond(" {\"message\": \"Authentication failed!\"} ")
        }
    }
}
