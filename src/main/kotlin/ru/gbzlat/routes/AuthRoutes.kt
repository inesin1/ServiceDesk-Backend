package ru.gbzlat.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import ru.gbzlat.authentication.Authentication
import ru.gbzlat.db.Users
import ru.gbzlat.db.loadUsers
import ru.gbzlat.dto.AuthRequest
import ru.gbzlat.dto.AuthResponse

fun Route.authRoute() {
    post("/auth") {
        val body = call.receive<AuthRequest>()

        val user = transaction {
            val id = Users.select(Users.id)
                .where { (Users.login eq body.login) and (Users.password eq body.password) }
                .singleOrNull()?.get(Users.id)

            id?.let { loadUsers(listOf(it), withDepartments = true)[it] }
        } ?: return@post call.respond(HttpStatusCode.Unauthorized, "Неверный логин или пароль")

        call.respond(AuthResponse(user, Authentication.instance.createAccessToken(user.id)))
    }
}
