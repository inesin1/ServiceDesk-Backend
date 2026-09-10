package ru.gbzlat.routes

import com.github.kotlintelegrambot.entities.ChatId
import io.github.smiley4.ktoropenapi.get
import io.github.smiley4.ktoropenapi.post
import io.github.smiley4.ktoropenapi.put
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import ru.gbzlat.db.Statuses
import ru.gbzlat.db.TicketCategories
import ru.gbzlat.db.TicketSources
import ru.gbzlat.db.findRef
import ru.gbzlat.dto.*
import ru.gbzlat.security.Role
import ru.gbzlat.security.UserPrincipal
import ru.gbzlat.security.requireRole
import ru.gbzlat.service.TicketFilter
import ru.gbzlat.service.TicketService
import ru.gbzlat.tgbot

private fun ids(raw: String?): List<Int>? =
    raw?.takeIf { it.isNotBlank() }?.split(",")?.map { it.trim().toInt() }

fun Route.ticketRoute() {
    route("/tickets") {
        get({
            operationId = "listTickets"
            summary = "Список заявок"
            description = "Сотрудник видит только свои заявки. Списки фильтров — id через запятую."
            tags = listOf("Заявки")
            request {
                queryParameter<Int>("limit") { required = false }
                queryParameter<Long>("offset") { required = false }
                queryParameter<String>("statuses") { required = false }
                queryParameter<String>("categories") { required = false }
                queryParameter<String>("creators") { required = false }
                queryParameter<String>("executors") { required = false }
                queryParameter<String>("departments") { required = false }
            }
            response {
                code(HttpStatusCode.OK) {
                    description = "Успешно"
                    body<Page<TicketResponse>>()
                }
            }
        }) {
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
        post({
            operationId = "createTicket"
            summary = "Создать заявку"
            description = "creatorId учитывается только для администратора, остальным подставляется автор токена."
            tags = listOf("Заявки")
            request { body<TicketDTO>() }
            response {
                code(HttpStatusCode.Created) {
                    description = "Заявка создана"
                }
                code(HttpStatusCode.Conflict) {
                    description = "Ссылка на несуществующую запись"
                    body<ErrorResponse>()
                }
            }
        }) {
            val principal = call.principal<UserPrincipal>()!!
            val body = call.receive<TicketDTO>()
            val creatorId = if (principal.role == Role.ADMIN) body.creatorId else principal.id

            val creatorName = TicketService.create(body, creatorId)
            notifySpecialists(body, creatorName)

            call.respond(HttpStatusCode.Created)
        }
        route("/{id}") {
            get({
                operationId = "getTicket"
                summary = "Заявка по id"
                tags = listOf("Заявки")
                request { pathParameter<Int>("id") }
                response {
                    code(HttpStatusCode.OK) {
                        description = "Успешно"
                        body<TicketResponse>()
                    }
                    code(HttpStatusCode.NotFound) {
                        description = "Не найдено"
                        body<ErrorResponse>()
                    }
                }
            }) {
                call.respond(TicketService.byId(call.parameters["id"]!!.toInt()))
            }
            requireRole(Role.SPECIALIST, Role.ADMIN) {
                put("/work/{executorId}", {
                    operationId = "assignTicketExecutor"
                    summary = "Назначить исполнителя"
                    description = "Переводит заявку в статус «В работе»."
                    tags = listOf("Заявки")
                    request {
                        pathParameter<Int>("id")
                        pathParameter<Int>("executorId")
                    }
                    response {
                        code(HttpStatusCode.NoContent) {
                            description = "Исполнитель назначен"
                        }
                        code(HttpStatusCode.NotFound) {
                            description = "Не найдено"
                            body<ErrorResponse>()
                        }
                    }
                }) {
                    TicketService.assignExecutor(
                        call.parameters["id"]!!.toInt(),
                        call.parameters["executorId"]!!.toInt(),
                    )
                    call.respond(HttpStatusCode.NoContent)
                }
                put("/close", {
                    operationId = "closeTicket"
                    summary = "Закрыть заявку"
                    tags = listOf("Заявки")
                    request { pathParameter<Int>("id") }
                    response {
                        code(HttpStatusCode.NoContent) {
                            description = "Заявка закрыта"
                        }
                        code(HttpStatusCode.NotFound) {
                            description = "Не найдено"
                            body<ErrorResponse>()
                        }
                    }
                }) {
                    TicketService.close(call.parameters["id"]!!.toInt())
                    call.respond(HttpStatusCode.NoContent)
                }
            }

            ticketCommentRoute()
        }

        refRoutes("/statuses", Statuses, "Статусы заявок", "TicketStatus", "TicketStatuses")
        refRoutes("/sources", TicketSources, "Источники заявок", "TicketSource", "TicketSources")
        refRoutes("/categories", TicketCategories, "Категории заявок", "TicketCategory", "TicketCategories")
    }
}

fun Route.ticketCommentRoute() {
    route("/comments") {
        get({
            operationId = "listTicketComments"
            summary = "Комментарии к заявке"
            tags = listOf("Комментарии")
            request { pathParameter<Int>("id") }
            response {
                code(HttpStatusCode.OK) {
                    description = "Успешно"
                    body<List<TicketCommentResponse>>()
                }
            }
        }) {
            call.respond(TicketService.comments(call.parameters["id"]!!.toInt()))
        }
        post({
            operationId = "addTicketComment"
            summary = "Добавить комментарий"
            description = "Автор берётся из токена."
            tags = listOf("Комментарии")
            request {
                pathParameter<Int>("id")
                body<TicketCommentDTO>()
            }
            response {
                code(HttpStatusCode.Created) {
                    description = "Комментарий добавлен"
                }
            }
        }) {
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
