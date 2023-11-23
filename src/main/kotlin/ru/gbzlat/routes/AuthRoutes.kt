package ru.gbzlat.routes

import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.ktorm.dsl.eq
import org.ktorm.entity.find
import ru.gbzlat.authentication.Authentication
import ru.gbzlat.database
import ru.gbzlat.database.models.Users.users
import ru.gbzlat.dto.AuthRequest
import ru.gbzlat.dto.AuthResponse
import ru.gbzlat.plugins.objectMapper

fun Route.authRoute() {
    post ("/auth") {
        try {
            val authData = call.receive<AuthRequest>()
            val user = database.users.find { it.login eq authData.login }

            if (user == null) {
                call.respond("Неверный логин или пароль. Попробуйте еще раз!")
            }

            val token = Authentication.instance.createAccessToken(user!!.id)
            call.respond(
                objectMapper.writeValueAsString(
                    AuthResponse(
                        user,
                        token
                    )
                )
            )
        } catch (e: Exception) {
            println("Произошла ошибка: ${e.message}")
            call.respond("Произошла ошибка: ${e.message}")
        }
    }
}