package ru.gbzlat.plugins

import com.github.kotlintelegrambot.entities.ChatId
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.routing.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.request.*
import org.ktorm.dsl.*
import org.ktorm.entity.filter
import org.ktorm.entity.find
import org.ktorm.entity.forEach
import org.ktorm.entity.toList
import ru.gbzlat.database.models.*
import ru.gbzlat.database
import ru.gbzlat.database.models.pojo.*
import ru.gbzlat.tgbot
import java.io.File
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.util.*

fun Application.configureRouting() {
    routing {
        // Test
        get("/") {
            call.respond("sheeesh")
        }

        // Api
        route("/api") {
            authenticationRoute()
            authenticate ("auth-jwt") {
                usersRoute()
                ticketsRoute()
                departmentsRoute()
                commentsRoute()
                enumsRoute()
            }
        }
    }
}

fun Route.authenticationRoute() {
    post ("/auth") {
        val auth = call.receive<Auth>()
        val user = database.users.find { it.login eq auth.login }

        if (user != null) {

            if (user.password == auth.password) {
                val token = Authentication.instance.createAccessToken(user.id)
                call.respond(HttpStatusCode.OK,
                    "{" +
                            "\"id\": \"${user.id}\"," +
                            "\"name\": \"${user.name}\"," +
                            "\"role\": \"${user.role!!.id}\"," +
                            "\"token\": \"${token}\"" +
                            "}")
            }
            else
                call.respond(HttpStatusCode.NotAcceptable, "Неверный пароль")

        }
        else
            call.respond(HttpStatusCode.NotAcceptable, "Неверный логин")
    }
}

fun Route.usersRoute() {
    route ("/users") {
        get {
            call.respond(gson.toJson(database.users.toList()))
        }
        get ("/{id}") {
            call.respond(
                gson.toJson(
                    database.users.find { it.id eq call.parameters["id"]!!.toInt() }
                )
            )
        }
        get ("/current") {
            val userId = call.principal<UserPrincipal>()!!.id

            call.respond(
                gson.toJson(
                    database.users.find {
                        it.id eq userId
                    }))
        }
        get ("/it") {
            call.respond(gson.toJson(database.users.filter { (it.roleId eq 2) or (it.roleId eq 3) }.toList()))
        }
        get ("/checklogin/{login}") {
            if (database.users.find { it.login eq call.parameters["login"]!!.toString() } == null)
                call.respond("ok")
            else
                call.respond("err")
        }
        post {
            try {
                val user = call.receive<UserPojo>()

                database.database.insert(Users) {
                    set(it.name, user.name)
                    set(it.login, user.login)
                    set(it.password, user.password)
                    set(it.roleId, user.roleId)
                    set(it.departmentId, user.departmentId)
                    set(it.phoneNumber, user.phoneNumber)
                    set(it.tgChatId, user.tgChatId)
                }

                call.respond(HttpStatusCode.OK)
            } catch (e: Exception){
                call.respond(HttpStatusCode.NotAcceptable)
            }
        }
        post("/upload") {
            try {
                val rewrite = call.request.queryParameters["rewrite"] == "true"

                val multipartData = call.receiveMultipart()
                var fileName = ""
                lateinit var file: File

                multipartData.forEachPart { part ->
                    if (part is PartData.FileItem) {
                        fileName = if (rewrite) "users_upload_${LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)}_rw" else "users_upload_${LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)}"
                        val fileBytes = part.streamProvider().readBytes()
                        file = File("uploads/${fileName}")
                        file.writeBytes(fileBytes)
                    }
                    part.dispose()
                }

                if (rewrite) {
                    File("backup/users_${LocalDateTime.now()}").writeText(gson.toJson(database.users.toList()))
                    database.database.deleteAll(Users)
                }

                file.forEachLine {line ->
                    try {
                        val user = line.split('\t')

                        if (database.users.find { it.login eq user[1] } == null) {
                            database.database.insert(Users) {
                                set(it.name, user[0])
                                set(it.login, user[1])
                                set(it.password, user[2])
                                set(it.roleId, database.roles.toList().find { role -> role.name == user[3] }!!.id)
                                set(it.departmentId, database.departments.toList().find { dep -> dep.name == user[4] }!!.id)
                                set(it.phoneNumber, user[5])
                                set(it.tgChatId, user[6].toLong())
                            }
                        } else {
                            database.database.update(Users) {
                                set(it.name, user[0])
                                set(it.password, user[2])
                                set(it.roleId, database.roles.toList().find { role -> role.name == user[3] }!!.id)
                                set(it.departmentId, database.departments.toList().find { dep -> dep.name == user[4] }!!.id)
                                set(it.phoneNumber, user[5])
                                set(it.tgChatId, user[6].toLong())
                                where {
                                    it.login eq user[1]
                                }
                            }
                        }
                    } catch (e: Exception) {
                        println("Ошибка при добавлении пользователя: ${e}")
                    }
                }

                call.respond(HttpStatusCode.OK, "ok")
            } catch (e: Exception) {
                println(e.message)
                call.respond(HttpStatusCode.NotAcceptable, "error: ${e.message}")
            }
        }
        put("/{id}") {
            try {
                val user = call.receive<UserPojo>()
                val userId = call.parameters["id"]!!.toInt()

                database.database.update(Users) {
                    set(it.name, user.name)
                    set(it.login, user.login)
                    set(it.password, user.password)
                    set(it.roleId, user.roleId)
                    set(it.departmentId, user.departmentId)
                    set(it.phoneNumber, user.phoneNumber)
                    set(it.tgChatId, user.tgChatId)
                    where {
                        it.id eq userId
                    }
                }

                call.respond(HttpStatusCode.OK, userId)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.NotAcceptable, e.message!!)
            }
        }
        put("/{id}/changePassword") {
            try {
                val newPassword = call.receiveText()
                val userId = call.parameters["id"]!!.toInt()

                database.database.update(Users) {
                    set(it.password, newPassword)
                    where {
                        it.id eq userId
                    }
                }
                call.respond(HttpStatusCode.OK, userId)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.NotAcceptable, e.message!!)
            }
        }
        delete("/{id}") {
            database.database.delete(Users) { it.id eq call.parameters["id"]!!.toInt() }
            call.respond(HttpStatusCode.OK)
        }
    }
}

fun Route.ticketsRoute() {
    route("/tickets") {

        // Возвращает заявки
        get {
            val userId = call.principal<UserPrincipal>()!!.id
            val user = database.users.find { it.id eq userId }!!

            when (user.roleId) {
                1 -> call.respond(gson.toJson(database.tickets.filter { it.creatorId eq userId}.toList()))    // Сотрудник
                2 -> call.respond(gson.toJson(database.tickets.toList().filter { ticket ->                    // ИТ-специалист
                    val creator = database.users.find { it.id eq ticket.creatorId }!!
                    return@filter creator.department!!.divisionId == user.department!!.divisionId ||
                            user.id == ticket.executorId
                }))
                3 -> call.respond(gson.toJson(database.tickets.toList()))                           // Админ
            }
        }

        // Возвращает заявку по id
        get ("/{id}") {
            call.respond(
                gson.toJson(
                    database.tickets.find {
                        it.id eq call.parameters["id"]!!.toInt()
                    }))
        }

        // Создает новую заявку
        post {
            try {
                val ticket = call.receive<TicketPojo>()
                val user = database.users.find { it.id eq call.principal<UserPrincipal>()!!.id }!!

                database.database.insert(Tickets) {
                    set(it.creatorId, user.id)
                    set(it.details, ticket.details)
                    set(it.categoryId, ticket.categoryId)
                    set(it.timeLimit, LocalDateTime.now(TimeZone.getTimeZone("GMT+5").toZoneId()).plusDays(1))
                    set(it.createDate, LocalDateTime.now(TimeZone.getTimeZone("GMT+5").toZoneId()))
                }

                database.users.filter { ((it.roleId eq 3) or (it.roleId eq 2)) and (it.departmentId eq user.departmentId) }.forEach {
                    if (it.tgChatId != null) {
                        tgbot.sendMessage(
                            ChatId.fromId(it.tgChatId),
                            text = """
                        Новая заявка
                        
                        Создатель: ${user.name}
                        Категория: ${database.problemCategories.find { it.id eq ticket.categoryId }?.name}
                        Подробности: ${ticket.details}
                    """.trimIndent()
                        )
                    }
                }

                call.respond(HttpStatusCode.OK)
            } catch (e: Exception){
                call.respond(HttpStatusCode.NotAcceptable)
            }
        }

        // Обновление данных
        route ("/{id}"){
            put ("/work/{executorId}") {
                database.database.update(Tickets) {
                    set(it.executorId, call.parameters["executorId"]!!.toInt())
                    set(it.statusId, 3)
                    where {
                        it.id eq call.parameters["id"]!!.toInt()
                    }
                }

                call.respond(HttpStatusCode.OK)
            }

            put ("/close") {
                database.database.update(Tickets) {
                    set(it.statusId, 2)
                    set(it.closeDate, LocalDateTime.now(TimeZone.getTimeZone("GMT+5").toZoneId()))
                    where {
                        it.id eq call.parameters["id"]!!.toInt()
                    }
                }

                call.respond(HttpStatusCode.OK)
            }
        }
    }
}

fun Route.enumsRoute() {
    route("/statuses"){
        get {
            call.respond(database.statuses.toList())
        }
        get("/{id}") {
            call.respond(
                gson.toJson(
                    database.statuses.find {
                        it.id eq call.parameters["id"]!!.toInt()
                    }
                )
            )
        }
        post {
            try {
                val status = call.receive<EnumPojo>()

                database.database.insert(Statuses) {
                    set(it.name, status.name)
                }

                call.respond(HttpStatusCode.OK);
            } catch (e: Exception){
                call.respond(HttpStatusCode.NotAcceptable)
            }
        }
        delete("/{id}") {
            database.database.delete(Statuses) { it.id eq call.parameters["id"]!!.toInt() }
            call.respond(HttpStatusCode.OK)
        }
    }

    route("/priorities"){
        get {
            call.respond(database.priorities.toList())
        }
        get("/{id}") {
            call.respond(
                gson.toJson(
                    database.priorities.toList().singleOrNull {
                        it.id == call.parameters["id"]?.toInt()
                    }
                )
            )
        }
        post {
            try {
                val priority = call.receive<EnumPojo>()

                database.database.insert(Priorities) {
                    set(it.name, priority.name)
                }

                call.respond(HttpStatusCode.OK);
            } catch (e: Exception){
                call.respond(HttpStatusCode.NotAcceptable)
            }
        }
        delete("/{id}") {
            database.database.delete(Priorities) { it.id eq call.parameters["id"]!!.toInt() }
            call.respond(HttpStatusCode.OK)
        }
    }

    route("/roles"){
        get {
            call.respond(database.roles.toList())
        }
        get("/{id}") {
            call.respond(
                gson.toJson(
                    database.roles.find {
                        it.id eq call.parameters["id"]!!.toInt()
                    }
                )
            )
        }
        post {
            try {
                val role = call.receive<EnumPojo>()

                database.database.insert(Roles) {
                    set(it.name, role.name)
                }

                call.respond(HttpStatusCode.OK);
            } catch (e: Exception){
                call.respond(HttpStatusCode.NotAcceptable)
            }
        }
        delete("/{id}") {
            database.database.delete(Roles) { it.id eq call.parameters["id"]!!.toInt() }
            call.respond(HttpStatusCode.OK)
        }
    }

    route("/problemCategories") {
        get { call.respond(database.problemCategories.toList()) }
        get("/{id}") {
            call.respond(
                gson.toJson(
                    database.problemCategories.find {
                        it.id eq call.parameters["id"]!!.toInt()
                    }
                )
            )
        }
        post {
            try {
                val problemCategory = call.receive<EnumPojo>()

                database.database.insert(ProblemCategories) {
                    set(it.name, problemCategory.name)
                }

                call.respond(HttpStatusCode.OK);
            } catch (e: Exception){
                call.respond(HttpStatusCode.NotAcceptable)
            }
        }
        delete("/{id}") {
            database.database.delete(ProblemCategories) { it.id eq call.parameters["id"]!!.toInt() }
            call.respond(HttpStatusCode.OK)
        }
    }

    route("/divisions"){
        get {
            call.respond(database.divisions.toList())
        }
        get("/{id}") {
            call.respond(
                gson.toJson(
                    database.divisions.find {
                        it.id eq call.parameters["id"]!!.toInt()
                    }
                )
            )
        }
        post {
            try {
                val division = call.receive<EnumPojo>()

                database.database.insert(Divisions) {
                    set(it.name, division.name)
                }

                call.respond(HttpStatusCode.OK);
            } catch (e: Exception){
                call.respond(HttpStatusCode.NotAcceptable)
            }
        }
        delete("/{id}") {
            database.database.delete(Divisions) { it.id eq call.parameters["id"]!!.toInt() }
            call.respond(HttpStatusCode.OK)
        }
    }
}

fun Route.departmentsRoute() {
    route("/departments") {
        get { call.respond(database.departments.toList()) }
        get ("/{id}") {
            call.respond(
                gson.toJson(
                    database.departments.find { it.id eq call.parameters["id"]!!.toInt() }
                )
            )
        }
        post {
            try {
                val department = call.receive<DepartmentPojo>()

                database.database.insert(Departments) {
                    set(it.divisionId, department.divisionId)
                    set(it.name, department.name)
                }

                call.respond(HttpStatusCode.OK);
            } catch (e: Exception){
                call.respond(HttpStatusCode.NotAcceptable)
            }
        }
        delete("/{id}") {
            database.database.delete(Departments) { it.id eq call.parameters["id"]!!.toInt() }
            call.respond(HttpStatusCode.OK)
        }
    }
}

fun Route.commentsRoute() {
    route("/comments") {
        get("/ticket/{id}") {
            val ticketId = call.parameters["id"]!!.toInt()

            call.respond(HttpStatusCode.OK,
                gson.toJson(
                    database.database
                        .from(Comments)
                        .select()
                        .where { Comments.ticketId eq ticketId }
                        .map { row -> Comment(row[Comments.id]!!, row[Comments.ticketId]!!, row[Comments.userId]!!,row[Comments.text]!!, row[Comments.createDate]!!) }
                )
            )
        }
        post {
            val comment = call.receive<CommentPojo>()

            val id = database.database.insertAndGenerateKey(Comments) {
                set(it.ticketId, comment.ticketId)
                set(it.userId, comment.userId)
                set(it.text, comment.text)
                set(it.createDate, LocalDateTime.now())
            }

            call.respond(HttpStatusCode.OK, id)
        }
    }
}