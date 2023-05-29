package ru.gbzlat.plugins

import io.ktor.http.*
import io.ktor.server.routing.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.request.*
import org.ktorm.dsl.eq
import org.ktorm.dsl.insert
import org.ktorm.dsl.update
import org.ktorm.entity.toList
import ru.gbzlat.database.models.*
import ru.gbzlat.database.models.pojo.DepartmentPojo
import ru.gbzlat.database.models.pojo.TicketPojo
import ru.gbzlat.database
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
            val userId = call.principal<UserIdPrincipalForUser>()!!.id

            call.respond(
                gson.toJson(
                    database.users.toList().singleOrNull {
                        it.id == userId
                    }))
        }
        post {
            // TODO post request users
        }
    }
}

fun Route.ticketsRoute() {
    route("/tickets") {

        // Возвращает заявки
        get {
            val userId = call.principal<UserIdPrincipalForUser>()!!.id
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

                val user = database.users.toList().single { it.id == call.principal<UserIdPrincipalForUser>()!!.id }
/*                val executor = db.users.toList().first { it.department!!.divisionId == user.department!!.divisionId && it.role!!.id == 2}*/

                database.database.insert(Tickets) {
                    set(it.creatorId, user.id)
                    /*set(it.executorId, executor.id)*/
                    set(it.details, ticket.details)
                    set(it.categoryId, ticket.categoryId)
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
    }
}