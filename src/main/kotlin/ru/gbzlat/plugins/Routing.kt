package ru.gbzlat.plugins

import com.auth0.jwt.JWT
import io.ktor.http.*
import io.ktor.server.routing.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.server.request.*
import io.ktor.server.sessions.*
import org.ktorm.dsl.insert
import org.ktorm.entity.toList
import ru.gbzlat.database.models.*
import ru.gbzlat.db
import java.time.LocalDateTime

fun Application.configureRouting() {
    routing {
        // Test
        get("/") {
            call.respond("sheeesh")
        }

        // Api
        route("/api") {
            authenticationRoute()
            authenticate ("auth-jwt") {
                usersRoute()
                ticketsRoute()
                enumsRoute()
            }
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

        //call.response.header(HttpHeaders.AccessControlAllowOrigin, "*")

        if (user != null) {
            val token = Authentication.instance.createAccessToken(user.id)
            //call.sessions.set(AuthorizationHeader(token))
            //call.response.cookies.append("token", token)
            call.respond("{\"token\": \"${token}\"}")
        }
        else
            call.respond(" {\"token\": \"null\"} ")
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
            val principal = call.principal<UserIdPrincipalForUser>()
            val userId = principal!!.id
            val userRoleId = db.users.toList().single{it.id == userId}.roleId

            when (userRoleId) {
                1 -> call.respond(gson.toJson(db.tickets.toList().filter { it.creatorId ==  userId}))
                2 -> call.respond(gson.toJson(db.tickets.toList().filter { it.executorId ==  userId}))
                3 -> call.respond(gson.toJson(db.tickets.toList()))
            }
        }
        get ("/{id}") {
            call.respond(
                gson.toJson(
                    db.tickets.toList().singleOrNull {
                        it.id == call.parameters["id"]?.toInt()
                    }))
        }
        post {
            val ticket = call.receive<TicketPojo>()
            db.database.insert(Tickets) {
                set(it.creatorId, ticket.creatorId)
                set(it.executorId, ticket.executorId)
                set(it.text, ticket.text)
                set(it.createDate, LocalDateTime.now())
                set(it.closeDate, null)
                set(it.priorityId, ticket.priorityId)
                set(it.statusId, ticket.statusId)
            }
        }
    }
}

fun Route.enumsRoute() {
    route("/statuses"){
        get {
            call.respond(db.statuses.toList())
        }
        get("/{id}") {
            call.respond(
                gson.toJson(
                    db.statuses.toList().singleOrNull {
                        it.id == call.parameters["id"]?.toInt()
                    }
                )
            )
        }
    }

    route("/priorities"){
        get {
            call.respond(db.priorities.toList())
        }
        get("/{id}") {
            call.respond(
                gson.toJson(
                    db.priorities.toList().singleOrNull {
                        it.id == call.parameters["id"]?.toInt()
                    }
                )
            )
        }
    }
}