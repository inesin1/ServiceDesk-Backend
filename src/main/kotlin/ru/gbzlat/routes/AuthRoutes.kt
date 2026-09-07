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
import ru.gbzlat.authentication.Role
import ru.gbzlat.authentication.verifyPassword
import ru.gbzlat.db.Users
import ru.gbzlat.db.loadUsers
import ru.gbzlat.dto.AuthRequest
import ru.gbzlat.dto.AuthResponse

fun Route.authRoute() {
    post("/auth") {
        val body = call.receive<AuthRequest>()

        val user = transaction {
            val row = Users.select(Users.id, Users.password)
                .where { Users.login eq body.login }
                .singleOrNull()
                ?: return@transaction null

            if (!verifyPassword(body.password, row[Users.password])) return@transaction null

            val id = row[Users.id]
            loadUsers(listOf(id), withDepartments = true)[id]
        } ?: return@post call.respond(HttpStatusCode.Unauthorized, "Неверный логин или пароль")

        val role = Role.byId(user.role.id)!!
        call.respond(AuthResponse(user, Authentication.instance.createAccessToken(user.id, role)))
    }
}
