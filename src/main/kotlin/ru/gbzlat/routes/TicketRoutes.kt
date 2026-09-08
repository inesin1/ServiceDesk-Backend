package ru.gbzlat.routes

import com.github.kotlintelegrambot.entities.ChatId
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import ru.gbzlat.db.Statuses
import ru.gbzlat.db.TicketCategories
import ru.gbzlat.db.TicketSources
import ru.gbzlat.db.findRef
import ru.gbzlat.dto.TicketCommentDTO
import ru.gbzlat.dto.TicketDTO
import ru.gbzlat.security.Role
import ru.gbzlat.security.UserPrincipal
import ru.gbzlat.security.requireRole
import ru.gbzlat.service.TicketFilter
import ru.gbzlat.service.TicketService
import ru.gbzlat.tgbot
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

private fun ids(raw: String?): List<Int>? =
    raw?.takeIf { it.isNotBlank() }?.split(",")?.map { it.trim().toInt() }

fun Route.ticketRoute() {
    route("/tickets") {
        get {
            val params = call.request.queryParameters
            val principal = call.principal<UserPrincipal>()!!

            val page = TicketService.list(
                userId = principal.id,
                role = principal.role,
                filter = TicketFilter(
                    statuses = ids(params["statuses"]),
                    categories = ids(params["categories"]),
                    creators = ids(params["creators"]),
                    executors = ids(params["executors"]),
                    departments = ids(params["departments"]),
                ),
                limit = params["limit"]?.toInt() ?: 100,
                offset = params["offset"]?.toLong() ?: 0,
            )

            call.respond(page)
        }
        post {
            val principal = call.principal<UserPrincipal>()!!
            val body = call.receive<TicketDTO>()
            val creatorId = if (principal.role == Role.ADMIN) body.creatorId else principal.id

            val creatorName = TicketService.create(body, creatorId)
            notifySpecialists(body, creatorName)

            call.respond(HttpStatusCode.Created)
        }
        route("/{id}") {
            get {
                call.respond(TicketService.byId(call.parameters["id"]!!.toInt()))
            }
            requireRole(Role.SPECIALIST, Role.ADMIN) {
                put("/work/{executorId}") {
                    TicketService.assignExecutor(
                        call.parameters["id"]!!.toInt(),
                        call.parameters["executorId"]!!.toInt(),
                    )
                    call.respond(HttpStatusCode.NoContent)
                }
                put("/close") {
                    TicketService.close(call.parameters["id"]!!.toInt())
                    call.respond(HttpStatusCode.NoContent)
                }
            }

            ticketCommentRoute()
        }

        refRoutes("/statuses", Statuses)
        refRoutes("/sources", TicketSources)
        refRoutes("/categories", TicketCategories)
    }
}

fun Route.ticketCommentRoute() {
    route("/comments") {
        get {
            call.respond(TicketService.comments(call.parameters["id"]!!.toInt()))
        }
        post {
            val body = call.receive<TicketCommentDTO>()
            TicketService.addComment(
                ticketId = call.parameters["id"]!!.toInt(),
                authorId = call.principal<UserPrincipal>()!!.id,
                text = body.text,
            )
            call.respond(HttpStatusCode.Created)
        }
    }
}

private fun notifySpecialists(ticket: TicketDTO, creatorName: String) {
    val bot = tgbot ?: return
    val categoryName = transaction { TicketCategories.findRef(ticket.categoryId)?.name }

    val text = """
        Новая заявка

        Создатель: $creatorName
        Категория: $categoryName
        Подробности: ${ticket.details}
    """.trimIndent()

    TicketService.specialistChatIds().forEach { bot.sendMessage(ChatId.fromId(it), text) }
}
