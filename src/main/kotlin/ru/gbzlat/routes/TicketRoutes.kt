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
import ru.gbzlat.database.models.TicketSources.ticketSources
import ru.gbzlat.plugins.objectMapper
import ru.gbzlat.tgbot
import java.time.LocalDateTime
import java.util.*

fun Route.ticketRoute() {
    route("/tickets") {
        get {
            try {
                val params = call.request.queryParameters

                val userId = call.principal<UserPrincipal>()!!.id
                val user = database.users.find { it.id eq userId }!!

                val limit = params["limit"]?.toInt()
                val offset = params["offset"]?.toInt()

                val filter: MutableMap<String, List<String>?> = mutableMapOf()
                filter["creators"] = params["creators"]?.let {
                    if (it != "")
                        it.split(",")
                    else
                        null
                }
                filter["executors"] = params["executors"]?.let {
                    if (it != "")
                        it.split(",")
                    else
                        null
                }
                filter["categories"] = params["categories"]?.let {
                    if (it != "")
                        it.split(",")
                    else
                        null
                }
                filter["statuses"] = params["statuses"]?.let {
                    if (it != "")
                        it.split(",")
                    else
                        null
                }
                filter["departments"] = params["departments"]?.let {
                    if (it != "")
                        it.split(",")
                    else
                        null
                }

                var tickets = when (user.role.id) {
                    // Сотрудник
                    1 -> database.tickets.filter {
                        it.creatorId eq userId
                    }
                        .drop(offset?:0)
                        .take(limit?:1000)
                        .sortedByDescending { it.id }


                    // Админ
                    2, 3 -> database.tickets
                        .drop(offset?:0)
                        .take(limit?:100)
                        .sortedByDescending { it.id }

                    else -> null
                }

                // Фильтр
                if (filter["statuses"] != null) {
                    tickets = tickets!!.filter {
                        it.statusId.inList(
                            filter["statuses"]!!.map { s -> s.toInt() }
                        )
                    }
                }

                if (filter["executors"] != null) {
                    tickets = tickets!!.filter {
                        it.executorId.inList(
                            filter["executors"]!!.map { s -> s.toInt() }
                        )
                    }
                }

                if (filter["categories"] != null) {
                    tickets = tickets!!.filter {
                        it.categoryId.inList(
                            filter["categories"]!!.map { s -> s.toInt() }
                        )
                    }
                }

                if (filter["creators"] != null) {
                    tickets = tickets!!.filter {
                        it.creatorId.inList(
                            filter["creators"]!!.map { s -> s.toInt() }
                        )
                    }
                }

/*                if (filter["departments"] != null) {
                    database.users.filter {
                        it.
                    }

                    tickets = tickets!!.filter {
                        it.creatorId.inList(
                            filter["departments"]!!.map { s -> s.toInt() }
                        )
                    }
                }*/


                call.respond(
                    objectMapper.writeValueAsString(
                        tickets!!.toList()
                    )
                )
            } catch (e: Exception) {
                println("Произошла ошибка: ${e.message}")
                call.respond("Произошла ошибка: ${e.message}")
            }
        }
        get ("/count") {
            val ticketsCount: Int = database.tickets
                .aggregateColumns { count() }!!

            call.respond(ticketsCount)
        }
        post {
            try {
                val ticketData = call.receive<TicketDTO>()
                val creator = database.users.find { it.id eq ticketData.creatorId }!!

                database.tickets.add(Ticket {
                    this.creator = creator
                    details = ticketData.details
                    source = database.ticketSources.find { it.id eq ticketData.sourceId }!!
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
        ticketSourceRoute()
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

fun Route.ticketSourceRoute() {
    route("/sources") {
        get {
            try {
                call.respond(
                    objectMapper.writeValueAsString(
                        database.ticketSources.toList()
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
                val ticketSource = database.ticketSources.find {
                    it.id eq id
                }

                if (ticketSource == null) {
                    call.respond(HttpStatusCode.NotFound)
                }

                call.respond(
                    objectMapper.writeValueAsString(
                        ticketSource!!
                    )
                )
            } catch (e: Exception) {
                println("Произошла ошибка: ${e.message}")
                call.respond("Произошла ошибка: ${e.message}")
            }
        }
        post {
            try {
                val ticketSourceData = call.receive<SimpleData>()

                database.ticketSources.add( TicketSource {
                    name = ticketSourceData.name
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

                database.delete(TicketSources) {
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
                    createdAt = LocalDateTime.now(TimeZone.getTimeZone("GMT+5").toZoneId())
                })

                call.respond(HttpStatusCode.Created)
            } catch (e: Exception) {
                println("Произошла ошибка: ${e.message}")
                call.respond("Произошла ошибка: ${e.message}")
            }
        }
    }
}
