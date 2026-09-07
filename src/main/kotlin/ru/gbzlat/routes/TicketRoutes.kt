package ru.gbzlat.routes

import com.github.kotlintelegrambot.entities.ChatId
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update
import ru.gbzlat.authentication.Role
import ru.gbzlat.authentication.UserPrincipal
import ru.gbzlat.authentication.requireRole
import ru.gbzlat.db.*
import ru.gbzlat.dto.TicketCommentDTO
import ru.gbzlat.dto.TicketDTO
import ru.gbzlat.tgbot
import java.time.LocalDateTime
import java.time.ZoneId

private val zone: ZoneId = ZoneId.of("GMT+5")

private const val STATUS_NEW = 1
private const val STATUS_CLOSED = 2
private const val STATUS_IN_WORK = 3

private fun ids(raw: String?): List<Int>? =
    raw?.takeIf { it.isNotBlank() }?.split(",")?.map { it.trim().toInt() }

fun Route.ticketRoute() {
    route("/tickets") {
        get {
            val params = call.request.queryParameters
            val userId = call.principal<UserPrincipal>()!!.id

            val tickets = transaction {
                val role = Users.select(Users.roleId).where { Users.id eq userId }
                    .single()[Users.roleId]

                var where: Op<Boolean> = Op.TRUE
                // Employees only ever see their own tickets.
                if (role == Role.EMPLOYEE.id) where = where and (Tickets.creatorId eq userId)
                ids(params["statuses"])?.let { where = where and (Tickets.statusId inList it) }
                ids(params["categories"])?.let { where = where and (Tickets.categoryId inList it) }
                ids(params["creators"])?.let { where = where and (Tickets.creatorId inList it) }
                ids(params["executors"])?.let { where = where and (Tickets.executorId inList it) }
                ids(params["departments"])?.let { departmentIds ->
                    val userIds = UserDepartments
                        .select(UserDepartments.userId)
                        .where { UserDepartments.departmentId inList departmentIds }
                        .map { it[UserDepartments.userId] }
                    where = where and (Tickets.creatorId inList userIds)
                }

                val rows = ticketsWithRefs()
                    .selectAll()
                    .where(where)
                    .orderBy(Tickets.id, SortOrder.DESC)
                    .limit(params["limit"]?.toInt() ?: 100)
                    .offset(params["offset"]?.toLong() ?: 0)
                    .toList()

                toTicketResponses(rows)
            }

            call.respond(tickets)
        }
        get("/count") {
            call.respond(transaction { Tickets.selectAll().count() })
        }
        post {
            val principal = call.principal<UserPrincipal>()!!
            val body = call.receive<TicketDTO>()
            val creator = if (principal.role == Role.ADMIN) body.creatorId else principal.id

            val creatorName = transaction {
                Tickets.insert {
                    it[creatorId] = creator
                    it[sourceId] = body.sourceId
                    it[categoryId] = body.categoryId
                    it[statusId] = STATUS_NEW
                    it[details] = body.details
                    it[createdAt] = LocalDateTime.now(zone)
                    it[timeLimit] = LocalDateTime.now(zone).plusDays(1)
                }

                Users.select(Users.name).where { Users.id eq creator }.single()[Users.name]
            }

            notifySpecialists(body, creatorName)
            call.respond(HttpStatusCode.Created)
        }
        route("/{id}") {
            get {
                val id = call.parameters["id"]!!.toInt()
                val ticket = transaction {
                    toTicketResponses(ticketsWithRefs().selectAll().where { Tickets.id eq id }.toList())
                }.singleOrNull() ?: return@get call.respond(HttpStatusCode.NotFound)

                call.respond(ticket)
            }
            requireRole(Role.SPECIALIST, Role.ADMIN) {
            put("/work/{executorId}") {
                val ticketId = call.parameters["id"]!!.toInt()
                val executor = call.parameters["executorId"]!!.toInt()

                val updated = transaction {
                    Tickets.update({ Tickets.id eq ticketId }) {
                        it[executorId] = executor
                        it[statusId] = STATUS_IN_WORK
                    }
                }

                if (updated == 0) return@put call.respond(HttpStatusCode.NotFound)
                call.respond(HttpStatusCode.NoContent)
            }
            put("/close") {
                val id = call.parameters["id"]!!.toInt()

                val updated = transaction {
                    Tickets.update({ Tickets.id eq id }) {
                        it[statusId] = STATUS_CLOSED
                        it[closedAt] = LocalDateTime.now(zone)
                    }
                }

                if (updated == 0) return@put call.respond(HttpStatusCode.NotFound)
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

private fun notifySpecialists(ticket: TicketDTO, creatorName: String) {
    val (categoryName, chatIds) = transaction {
        val category = TicketCategories.findRef(ticket.categoryId)?.name
        val chats = Users
            .select(Users.tgChatId)
            .where { (Users.roleId neq Role.EMPLOYEE.id) and Users.tgChatId.isNotNull() }
            .mapNotNull { it[Users.tgChatId] }
        category to chats
    }

    val text = """
        Новая заявка

        Создатель: $creatorName
        Категория: $categoryName
        Подробности: ${ticket.details}
    """.trimIndent()

    val bot = tgbot ?: return
    chatIds.forEach { bot.sendMessage(ChatId.fromId(it), text) }
}

fun Route.ticketCommentRoute() {
    route("/comments") {
        get {
            val ticketId = call.parameters["id"]!!.toInt()
            val comments = transaction {
                toCommentResponses(
                    TicketComments.selectAll()
                        .where { TicketComments.ticketId eq ticketId }
                        .orderBy(TicketComments.id)
                        .toList()
                )
            }

            call.respond(comments)
        }
        post {
            val ticketId = call.parameters["id"]!!.toInt()
            val body = call.receive<TicketCommentDTO>()
            val author = call.principal<UserPrincipal>()!!.id

            transaction {
                TicketComments.insert {
                    it[TicketComments.ticketId] = ticketId
                    it[creatorId] = author
                    it[text] = body.text
                    it[createdAt] = LocalDateTime.now(zone)
                }
            }

            call.respond(HttpStatusCode.Created)
        }
    }
}
