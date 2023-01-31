package ru.gbzlat.plugins

import io.ktor.server.routing.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.request.*
import org.ktorm.entity.toList
import ru.gbzlat.database.models.Auth
import ru.gbzlat.database.models.users
import ru.gbzlat.db

fun Application.configureRouting() {
    routing {
        get("/") {
            call.respond("sheeesh")
        }
        post ("/api/auth") {
            val auth = call.receive<Auth>()
            val user = db.users.toList()
                .firstOrNull {
                    it.login == auth.login && it.password == auth.password
                }

            if (user != null)
                call.respond(" {\"message\": \"You're successfully authenticated!\"} ")
            else
                call.respond(" {\"message\": \"Authentication failed!\"} ")
        }
        get ("/api/users") {
            call.respond(gson.toJson(db.users.toList()))
        }
    }
}
