package ru.gbzlat.routes

import io.github.smiley4.ktoropenapi.post
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import ru.gbzlat.dto.AuthRequest
import ru.gbzlat.dto.AuthResponse
import ru.gbzlat.dto.ErrorResponse
import ru.gbzlat.error.ApiException
import ru.gbzlat.security.Authentication
import ru.gbzlat.service.AuthService

fun Route.authRoute() {
    post("/auth", {
        operationId = "login"
        summary = "Вход"
        description = "Возвращает пользователя и JWT. Токен живёт 12 часов."
        tags = listOf("Авторизация")
        protected = false
        request { body<AuthRequest>() }
        response {
            code(HttpStatusCode.OK) {
                description = "Успешно"
                body<AuthResponse>()
            }
            code(HttpStatusCode.Unauthorized) {
                description = "Требуется авторизация"
                body<ErrorResponse>()
            }
        }
    }) {
        val body = call.receive<AuthRequest>()
        val (user, role) = AuthService.authenticate(body.login, body.password)
            ?: throw ApiException(HttpStatusCode.Unauthorized, "Неверный логин или пароль")

        call.respond(AuthResponse(user, Authentication.instance.createAccessToken(user.id, role)))
    }
}
