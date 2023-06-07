package ru.gbzlat.plugins

import io.ktor.http.*
import io.ktor.server.routing.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.request.*
import org.ktorm.dsl.delete
import org.ktorm.dsl.eq
import org.ktorm.dsl.insert
import org.ktorm.dsl.update
import org.ktorm.entity.toList
import ru.gbzlat.database.models.*
import ru.gbzlat.database
import ru.gbzlat.database.models.pojo.*
import java.time.LocalDateTime

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
                enumsRoute()
            }
        }
    }
}

fun Route.authenticationRoute() {
    post ("/auth") {
        val auth = call.receive<Auth>()
        val user = database.users.toList()
            .firstOrNull {
                it.login == auth.login && it.password == auth.password
            }

        if (user != null) {
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
            call.respond(HttpStatusCode.NotAcceptable, "Неверный логин или пароль")
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
                    database.users.toList().singleOrNull {
                        it.id == call.parameters["id"]?.toInt()
                    }))
        }
        get ("/current") {
            val userId = call.principal<UserPrincipal>()!!.id

            call.respond(
                gson.toJson(
                    database.users.toList().singleOrNull {
                        it.id == userId
                    }))
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
            val user = database.users.toList().single{it.id == userId}

            when (user.roleId) {
                1 -> call.respond(gson.toJson(database.tickets.toList().filter { it.creatorId == userId}))    // Сотрудник
                2 -> call.respond(gson.toJson(database.tickets.toList().filter { ticket ->                     // ИТ-специалист
                    val creator = database.users.toList().single{it.id == ticket.creatorId}
                    return@filter creator.departmentId == user.departmentId
                }))
                3 -> call.respond(gson.toJson(database.tickets.toList()))                                     // Админ
            }
        }

        // Возвращает заявку по id
        get ("/{id}") {
            call.respond(
                gson.toJson(
                    database.tickets.toList().singleOrNull {
                        it.id == call.parameters["id"]?.toInt()
                    }))
        }

        // Создает новую заявку
        post {
            try {
                val ticket = call.receive<TicketPojo>()
                val user = database.users.toList().single { it.id == call.principal<UserPrincipal>()!!.id }

                database.database.insert(Tickets) {
                    set(it.creatorId, user.id)
                    set(it.details, ticket.details)
                    set(it.categoryId, ticket.categoryId)
                    set(it.timeLimit, LocalDateTime.now().plusDays(1))
                    set(it.createDate, LocalDateTime.now())
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
                    set(it.closeDate, LocalDateTime.now())
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
                    database.statuses.toList().singleOrNull {
                        it.id == call.parameters["id"]?.toInt()
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
                    database.roles.toList().singleOrNull {
                        it.id == call.parameters["id"]?.toInt()
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
                    database.problemCategories.toList().singleOrNull {
                        it.id == call.parameters["id"]?.toInt()
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
                    database.divisions.toList().singleOrNull {
                        it.id == call.parameters["id"]?.toInt()
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
                    database.departments.toList().singleOrNull{ it.id == call.parameters["id"]?.toInt() }
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