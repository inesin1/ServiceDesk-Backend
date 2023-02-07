package ru.gbzlat.plugins

import io.ktor.server.routing.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.request.*
import io.ktor.util.*
import org.ktorm.entity.toList
import ru.gbzlat.database.models.Auth
import ru.gbzlat.database.models.tickets
import ru.gbzlat.database.models.users
import ru.gbzlat.db

fun Application.configureRouting() {
    routing {
        // Test
        get("/") {
            call.respond("sheeesh")
        }

        // Api
        route("/api") {
            authenticationRoute()
            usersRoute()
            ticketsRoute()
        }
    }
}

fun Route.authenticationRoute() {
    post ("/auth") {
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
}

fun Route.usersRoute() {
    route ("/users") {
        get {
            call.respond(gson.toJson(db.users.toList()))
        }
        get ("/{id}") {
            call.respond(
                gson.toJson(
                    db.users.toList().singleOrNull {
                        it.id == call.parameters["id"]?.toInt()
                    }))
        }
        post {
            // TODO post request users
        }
    }
}

fun Route.ticketsRoute() {
    route("/tickets") {
        get {
            call.respond(gson.toJson(db.tickets.toList()))
        }
        get ("/{id}") {
            call.respond(
                gson.toJson(
                    db.tickets.toList().singleOrNull {
                        it.id == call.parameters["id"]?.toInt()
                    }))
        }
        post {
            // TODO post request tickets
        }
    }
}
