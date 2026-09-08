package ru.gbzlat.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import ru.gbzlat.dto.AuthRequest
import ru.gbzlat.dto.AuthResponse
import ru.gbzlat.error.ApiException
import ru.gbzlat.security.Authentication
import ru.gbzlat.service.AuthService

fun Route.authRoute() {
    post("/auth") {
        val body = call.receive<AuthRequest>()
        val (user, role) = AuthService.authenticate(body.login, body.password)
            ?: throw ApiException(HttpStatusCode.Unauthorized, "Неверный логин или пароль")

        call.respond(AuthResponse(user, Authentication.instance.createAccessToken(user.id, role)))
    }
}
