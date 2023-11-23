package ru.gbzlat.routes

import com.github.kotlintelegrambot.entities.ChatId
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.ktorm.dsl.*
import org.ktorm.entity.*
import ru.gbzlat.database
import ru.gbzlat.database.models.*
import ru.gbzlat.database.models.Statuses.statuses
import ru.gbzlat.database.models.TicketCategories.ticketCategories
import ru.gbzlat.database.models.TicketComments.ticketComments
import ru.gbzlat.database.models.Tickets.tickets
import ru.gbzlat.database.models.Users.users
import ru.gbzlat.dto.TicketCommentDTO
import ru.gbzlat.dto.SimpleData
import ru.gbzlat.dto.TicketDTO
import ru.gbzlat.authentication.UserPrincipal
import ru.gbzlat.plugins.objectMapper
import ru.gbzlat.tgbot
import java.time.LocalDateTime
import java.util.*

fun Route.ticketRoute() {
    route("/tickets") {
        get {
            try {
                val userId = call.principal<UserPrincipal>()!!.id
                val user = database.users.find { it.id eq userId }!!

                val limit = call.request.queryParameters["limit"]?.toInt()
                val offset = call.request.queryParameters["offset"]?.toInt()

                val tickets = when (user.role.id) {
                    // Сотрудник
                    1 -> database.tickets.filter {
                        it.creatorId eq userId
                    }
                        .drop(offset?:0)
                        .take(limit?:100)
                        .toList()

/*                    // ИТ-специалист
                    2 -> {
                        call.respond(
                            database.tickets.filter { ticket ->
                                val creator = database.users.find {
                                    it.id eq ticket.creatorId
                                }!!

                                return@filter user.id eq ticket.executorId
                            }
                        )
                    }*/

                    // Админ
                    2, 3 -> database.tickets
                        .drop(offset?:0)
                        .take(limit?:100)
                        .toList()

                    else -> null
                }

                if (tickets == null) {
                    call.respond(HttpStatusCode.NotFound)
                }

                call.respond(
                    objectMapper.writeValueAsString(
                        tickets!!
                    )
                )
            } catch (e: Exception) {
                println("Произошла ошибка: ${e.message}")
                call.respond("Произошла ошибка: ${e.message}")
            }
        }
        post {
            try {
                val ticketData = call.receive<TicketDTO>()
                val creator = database.users.find {
                    it.id eq call.principal<UserPrincipal>()!!.id
                }!!

                database.tickets.add(Ticket {
                    this.creator = creator
                    details = ticketData.details
                    category = database.ticketCategories.find { it.id eq ticketData.categoryId }!!
                    timeLimit =
                        LocalDateTime.now(
                            TimeZone.getTimeZone("GMT+5").toZoneId()
                        )
                            .plusDays(1)
                    createdAt = LocalDateTime.now(TimeZone.getTimeZone("GMT+5").toZoneId())
                })

                // Отправка сообщения в бота
                database.users.filter {
                    ((it.roleId eq 3) or (it.roleId eq 2))
                }
                    .map {user ->
                        if (user.tgChatId == null)
                            return@map

                        tgbot.sendMessage(
                            ChatId.fromId(user.tgChatId!!),
                            text = """
                                Новая заявка
                                
                                Создатель: ${creator.name}
                                Категория: ${database.ticketCategories.find { it.id eq ticketData.categoryId }?.name}
                                Подробности: ${ticketData.details}
                                """
                                .trimIndent()
                        )
                    }

                call.respond(HttpStatusCode.Created)
            } catch (e: Exception){
                println("Произошла ошибка: ${e.message}")
                call.respond("Произошла ошибка: ${e.message}")
            }
        }
        route ("/{id}"){
            get {
                try {
                    val id = call.parameters["id"]!!.toInt()
                    val ticket = database.tickets.find {
                        it.id eq id
                    }

                    if (ticket == null) {
                        call.respond(HttpStatusCode.NotFound)
                    }

                    call.respond(
                        objectMapper.writeValueAsString(
                            ticket!!
                        )
                    )
                } catch (e: Exception) {
                    println("Произошла ошибка: ${e.message}")
                    call.respond("Произошла ошибка: ${e.message}")
                }
            }
            put ("/work/{executorId}") {
                try {
                    val ticketId = call.parameters["id"]!!.toInt()
                    val executorId = call.parameters["executorId"]!!.toInt()

                    database.update(Tickets) {
                        set(it.executorId, executorId)
                        set(it.statusId, 3)
                        where {
                            it.id eq ticketId
                        }
                    }

                    call.respond(ticketId)
                } catch (e: Exception) {
                    println("Произошла ошибка: ${e.message}")
                    call.respond("Произошла ошибка: ${e.message}")
                }
            }
            put ("/close") {
                try {
                    val id = call.parameters["id"]!!.toInt()

                    database.update(Tickets) {
                        set(it.statusId, 2)
                        set(it.closedAt, LocalDateTime.now(TimeZone.getTimeZone("GMT+5").toZoneId()))
                        where {
                            it.id eq id
                        }
                    }

                    call.respond(id)
                } catch (e: Exception) {
                    println("Произошла ошибка: ${e.message}")
                    call.respond("Произошла ошибка: ${e.message}")
                }
            }

            ticketCommentRoute()
        }

        statusRoute()
        ticketCategoryRoute()
    }
}

fun Route.statusRoute() {
    route("/statuses"){
        get {
            try {
                call.respond(
                    objectMapper.writeValueAsString(
                        database.statuses.toList()
                    )
                )
            } catch (e: Exception) {
                println("Произошла ошибка: ${e.message}")
                call.respond("Произошла ошибка: ${e.message}")
            }
        }
        get("/{id}") {
            try {
                val id = call.parameters["id"]!!.toInt()
                val status = database.statuses.find {
                    it.id eq id
                }

                if (status == null) {
                    call.respond(HttpStatusCode.NotFound)
                }

                call.respond(
                    objectMapper.writeValueAsString(
                        status!!
                    )
                )
            } catch (e: Exception) {
                println("Произошла ошибка: ${e.message}")
                call.respond("Произошла ошибка: ${e.message}")
            }
        }
        post {
            try {
                val statusData = call.receive<SimpleData>()

                database.statuses.add(Status {
                    name = statusData.name
                })

                call.respond(HttpStatusCode.Created)
            } catch (e: Exception){
                println("Произошла ошибка: ${e.message}")
                call.respond("Произошла ошибка: ${e.message}")
            }
        }
        put ("/{id}") {
            try {
                val id = call.parameters["id"]!!.toInt()
                val statusDTO: SimpleData = call.receive<SimpleData>()

                database.update(Statuses) {
                        set(it.name, statusDTO.name)
                        where {
                            it.id eq id
                        }
                    }

                call.respond(id)
            } catch (e: Exception) {
                println("Произошла ошибка: ${e.message}")
                call.respond("Произошла ошибка: ${e.message}")
            }
        }
        delete("/{id}") {
            try {
                val id = call.parameters["id"]!!.toInt()

                database.delete(Statuses) {
                    it.id eq id
                }

                call.respond(id)
            } catch (e: Exception) {
                println("Произошла ошибка: ${e.message}")
                call.respond("Произошла ошибка: ${e.message}")
            }
        }
    }
}

/*fun Route.ticketPriorityRoute() {
    route("/tickets/priorities"){
        get {
            call.respond(ru.gbzlat.database.priorities.toList())
        }
        get("/{id}") {
            call.respond(
                gson.toJson(
                    ru.gbzlat.database.priorities.toList().singleOrNull {
                        it.id == call.parameters["id"]?.toInt()
                    }
                )
            )
        }
        post {
            try {
                val priority = call.receive<SimpleData>()

                ru.gbzlat.database.database.insert(Priorities) {
                    set(it.name, priority.name)
                }

                call.respond(HttpStatusCode.OK);
            } catch (e: Exception){
                call.respond(HttpStatusCode.NotAcceptable)
            }
        }
        delete("/{id}") {
            ru.gbzlat.database.database.delete(Priorities) { it.id eq call.parameters["id"]!!.toInt() }
            call.respond(HttpStatusCode.OK)
        }
    }
}*/

fun Route.ticketCategoryRoute() {
    route("/categories") {
        get {
            try {
                call.respond(
                    objectMapper.writeValueAsString(
                        database.ticketCategories.toList()
                    )
                )
            } catch (e: Exception) {
                println("Произошла ошибка: ${e.message}")
                call.respond("Произошла ошибка: ${e.message}")
            }
        }
        get("/{id}") {
            try {
                val id = call.parameters["id"]!!.toInt()
                val ticketCategory = database.ticketCategories.find {
                    it.id eq id
                }

                if (ticketCategory == null) {
                    call.respond(HttpStatusCode.NotFound)
                }

                call.respond(
                    objectMapper.writeValueAsString(
                        ticketCategory!!
                    )
                )
            } catch (e: Exception) {
                println("Произошла ошибка: ${e.message}")
                call.respond("Произошла ошибка: ${e.message}")
            }
        }
        post {
            try {
                val ticketCategoryData = call.receive<SimpleData>()

                database.ticketCategories.add( TicketCategory {
                    name = ticketCategoryData.name
                })

                call.respond(HttpStatusCode.Created)
            } catch (e: Exception){
                println("Произошла ошибка: ${e.message}")
                call.respond("Произошла ошибка: ${e.message}")
            }
        }
        delete("/{id}") {
            try {
                val id = call.parameters["id"]!!.toInt()

                database.delete(TicketCategories) {
                    it.id eq id
                }

                call.respond(id)
            } catch (e: Exception) {
                println("Произошла ошибка: ${e.message}")
                call.respond("Произошла ошибка: ${e.message}")
            }
        }
    }
}

fun Route.ticketCommentRoute() {
    route("/comments") {
        get {
            try {
                val ticketId = call.parameters["id"]!!.toInt()
                val comments = database.ticketComments
                    .filter {
                        it.ticketId eq ticketId
                    }
                    .toList()
                /*val comments = database
                    .from(TicketComments)
                    .select()
                    .where { TicketComments.ticketId eq ticketId }
                                .map { row ->
                                    TicketComment {
                                        id = row[TicketComments.id]!!
                                        ticketId = row[TicketComments.ticketId]!!
                                        creator = database.users.find { it.id eq row[TicketComments.creatorId]!! }!!
                                        text = row[TicketComments.text]!!
                                        createdAt = row[TicketComments.createdAt]!!
                                    }
                                }*/

                call.respond(
                    objectMapper.writeValueAsString(
                        comments
                    )
                )
            } catch (e: Exception) {
                println("Произошла ошибка: ${e.message}")
                call.respond("Произошла ошибка: ${e.message}")
            }
        }
        post {
            try {
                val id = call.parameters["id"]!!.toInt()
                val ticketCommentData = call.receive<TicketCommentDTO>()

                database.ticketComments.add(TicketComment {
                    ticketId = id
                    creator = database.users.find { it.id eq ticketCommentData.creatorId }!!
                    text = ticketCommentData.text
                    createdAt = LocalDateTime.now()
                })

                call.respond(HttpStatusCode.Created)
            } catch (e: Exception) {
                println("Произошла ошибка: ${e.message}")
                call.respond("Произошла ошибка: ${e.message}")
            }
        }
    }
}
