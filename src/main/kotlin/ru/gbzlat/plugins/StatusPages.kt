package ru.gbzlat.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import org.jetbrains.exposed.v1.exceptions.ExposedSQLException
import ru.gbzlat.error.ApiException
import ru.gbzlat.error.ErrorResponse

fun Application.configureStatusPages() {
    install(StatusPages) {
        exception<ApiException> { call, cause ->
            call.respond(cause.status, ErrorResponse(cause.status.value, cause.message))
        }
        exception<BadRequestException> { call, cause ->
            call.application.log.debug("Rejected request body on {}", call.request.local.uri, cause)
            call.respondError(HttpStatusCode.BadRequest, "Некорректное тело запроса")
        }
        exception<NumberFormatException> { call, _ ->
            call.respondError(HttpStatusCode.BadRequest, "Ожидалось число")
        }
        exception<ExposedSQLException> { call, cause ->
            call.application.log.warn("Database rejected the request", cause)
            call.respondError(HttpStatusCode.Conflict, "Запрос нарушает ограничения данных")
        }
        exception<Throwable> { call, cause ->
            call.application.log.error("Unhandled failure on ${call.request.local.uri}", cause)
            call.respondError(HttpStatusCode.InternalServerError, "Внутренняя ошибка сервера")
        }
    }
}

private suspend fun ApplicationCall.respondError(status: HttpStatusCode, message: String) =
    respond(status, ErrorResponse(status.value, message))
