package ru.gbzlat.routes

import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.ktorm.dsl.*
import org.ktorm.entity.*
import ru.gbzlat.database
import ru.gbzlat.database.models.*
import ru.gbzlat.database.models.Roles.roles
import ru.gbzlat.database.models.Users.users
import ru.gbzlat.dto.SimpleData
import ru.gbzlat.dto.UserDTO
import ru.gbzlat.authentication.UserPrincipal
import ru.gbzlat.database.models.UserDepartments.userDepartments
import ru.gbzlat.plugins.objectMapper
import java.io.File
import java.time.LocalDateTime
import java.time.ZoneOffset

fun Route.userRoute() {
    route ("/users") {
        get {
            try {
                val offset = call.request.queryParameters["offset"]?.toInt()
                val limit = call.request.queryParameters["limit"]?.toInt()
                val with = call.request.queryParameters["with"]?.split(",")

                val users = database.users
                    .drop(offset?:0)
                    .take(limit?:1000)
                    .toList()

                with?.map {
                    when (it) {
                        "departments" -> {
                            users.map {user ->
                                user.addDepartments()
                            }
                        }

                        else -> {}
                    }
                }

                call.respond(
                    objectMapper.writeValueAsString(
                        users
                    )
                )
            } catch (e: Exception) {
                println("Произошла ошибка: ${e.message}")
                call.respond("Произошла ошибка: ${e.message}")
            }
        }
        route ("/{id}") {
            get {
                try {
                    val id = call.parameters["id"]!!.toInt()
                    val user = database.users.find { it.id eq id }

                    if (user == null) {
                        call.respond(HttpStatusCode.NotFound)
                    }

                    val with = call.request.queryParameters["with"]?.split(",")
                    with?.map {
                        when (it) {
                            "departments" -> {
                                user!!.addDepartments()
                            }

                            else -> {}
                        }
                    }

                    call.respond(
                        objectMapper.writeValueAsString(
                            user
                        )
                    )
                } catch (e: Exception) {
                    println("Произошла ошибка: ${e.message}")
                    call.respond("Произошла ошибка: ${e.message}")
                }
            }

            userDepartmentsRoute()
        }
        get ("/current") {
            try {
                val userId = call.principal<UserPrincipal>()!!.id
                val currentUser = database.users.find {
                    it.id eq userId
                }

                if (currentUser == null) {
                    call.respond(HttpStatusCode.NotFound)
                }

                val with = call.request.queryParameters["with"]?.split(",")
                with?.map {
                    when (it) {
                        "departments" -> {
                            currentUser!!.addDepartments()
                        }

                        else -> {}
                    }
                }

                call.respond(
                    objectMapper.writeValueAsString(
                        currentUser
                    )
                )
            } catch (e: Exception) {
                println("Произошла ошибка: ${e.message}")
                call.respond("Произошла ошибка: ${e.message}")
            }
        }
        get ("/specialists") {
            try {
                val users = database.users
                    .filter {
                        (it.roleId eq 2) or (it.roleId eq 3)
                    }
                    .toList()

                val with = call.request.queryParameters["with"]?.split(",")
                with?.map {
                    when (it) {
                        "departments" -> {
                            users.map {user ->
                                user.addDepartments()
                            }
                        }

                        else -> {}
                    }
                }

                call.respond(
                    objectMapper.writeValueAsString(
                        users
                    )
                )
            } catch (e: Exception) {
                println("Произошла ошибка: ${e.message}")
                call.respond("Произошла ошибка: ${e.message}")
            }
        }
        get ("/checklogin/{login}") {
            if (database.users.find { it.login eq call.parameters["login"]!!.toString() } == null)
                call.respond("ok")
            else
                call.respond("err")
        }
        post {
            try {
                val userData = call.receive<UserDTO>()

                database.useTransaction {
                    val createdId = database.insertAndGenerateKey(Users) {
                        set(it.name, userData.name)
                        set(it.login, userData.login)
                        set(it.password, userData.password)
                        set(it.roleId, userData.roleId)
                        set(it.phone, userData.phone)
                        set(it.tgChatId, userData.tgChatId)
                    }
                    userData.departmentIds.map {departmentId ->
                        database.insert(UserDepartments) {
                            set(it.userId, createdId as Int)
                            set(it.departmentId, departmentId)
                        }
                    }
                }

                call.respond(HttpStatusCode.Created)
            } catch (e: Exception){
                println("Произошла ошибка: ${e.message}")
                call.respond("Произошла ошибка: ${e.message}")
            }
        }
        post("/upload") {
            try {
                val rewrite = call.request.queryParameters["rewrite"] == "true"

                val multipartData = call.receiveMultipart()
                lateinit var fileName: String
                lateinit var file: File

                multipartData.forEachPart { part ->
                    if (part is PartData.FileItem) {
                        fileName =
                            if (rewrite)
                                "users_upload_${LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)}_rw"
                            else
                                "users_upload_${LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)}"
                        val fileBytes = part.streamProvider().readBytes()
                        file = File("uploads/${fileName}")
                        file.writeBytes(fileBytes)
                    }
                    part.dispose()
                }

                if (rewrite) {
                    File("backup/users_${LocalDateTime.now()}")
                        .writeText(
                            objectMapper.writeValueAsString(
                                database.users.toList()
                            )
                        )
                    database.deleteAll(Users)
                }

                file.forEachLine {line ->
                    try {
                        val userArray = line.split('\t')
                        val user = User {
                            name = userArray[0]
                            login = userArray[1]
                            password = userArray[2]
                            role = database.roles.find { it.id eq userArray[3].toInt() }!!
                            phone = userArray[5]
                            tgChatId = userArray[6].toLong()
                        }

                        if (database.users.find { it.login eq userArray[1] } == null) {
                            database.users.add(user)
                        } else {
                            database.update(Users) {
                                set(it.name, user.name)
                                set(it.password, user.password)
                                set(it.roleId, user.role.id)
                                set(it.phone, user.phone)
                                set(it.tgChatId, user.tgChatId)
                                where {
                                    it.login eq user.login
                                }
                            }
                        }
                    } catch (e: Exception) {
                        println("Ошибка при добавлении пользователя: ${e.message}")
                    }
                }

                call.respond(HttpStatusCode.Created)
            } catch (e: Exception) {
                println("Произошла ошибка: ${e.message}")
                call.respond("Произошла ошибка: ${e.message}")
            }
        }
        put("/{id}") {
            try {
                val userData = call.receive<UserDTO>()
                val userId = call.parameters["id"]!!.toInt()
                val user = User {
                    id = userId
                    name = userData.name
                    login = userData.login
                    password = userData.password
                    role = database.roles.find { it.id eq userData.roleId }!!
                    phone = userData.phone
                    tgChatId = userData.tgChatId
                }

                database.useTransaction {
                    database.delete(UserDepartments) {
                        it.userId eq userId
                    }

                    userData.departmentIds.map {departmentId ->
                        database.insert(UserDepartments) {
                            set(it.userId, userId)
                            set(it.departmentId, departmentId)
                        }
                    }

                    database.users.update(user)
                }

                call.respond(userId)
            } catch (e: Exception) {
                println("Произошла ошибка: ${e.message}")
                call.respond("Произошла ошибка: ${e.message}")
            }
        }
        delete("/{id}") {
            try {
                val id = call.parameters["id"]!!.toInt()

                database.useTransaction {
                    database.delete(UserDepartments) {
                        it.userId eq id
                    }

                    database.delete(Users) {
                        it.id eq id
                    }
                }

                call.respond(id)
            } catch (e: Exception) {
                println("Произошла ошибка: ${e.message}")
                call.respond("Произошла ошибка: ${e.message}")
            }
        }

        roleRoute()
    }
}

// /users/roles
fun Route.roleRoute() {
    route("/roles"){
        get {
            try {
                call.respond(
                    objectMapper.writeValueAsString(
                        database.roles.toList()
                    )
                )
            } catch (e: Exception) {
                println("Произошла ошибка: ${e.message}")
                call.respond("Произошла ошибка: ${e.message}")
            }
        }
        get("/{id}") {
            try {
                val role = database.roles.find {
                    it.id eq call.parameters["id"]!!.toInt()
                }

                if (role == null) {
                    call.respond(HttpStatusCode.NotFound)
                }

                call.respond(
                    objectMapper.writeValueAsString(
                        role!!
                    )
                )
            } catch (e: Exception) {
                println("Произошла ошибка: ${e.message}")
                call.respond("Произошла ошибка: ${e.message}")
            }
        }
        post {
            try {
                val roleData = call.receive<SimpleData>()

                database.roles.add(Role {
                    name = roleData.name
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

                database.delete(Roles) {
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

// /users/{id}/departments
fun Route.userDepartmentsRoute() {
    route("/departments") {
        get {
            try {
                val userId = call.parameters["id"]!!.toInt()

                val departments = database.userDepartments
                    .filter { it.userId eq userId }
                    .map { it.department }

                call.respond(
                    objectMapper.writeValueAsString(
                        departments
                    )
                )
            } catch (e: Exception) {
                println("Произошла ошибка: ${e.message}")
                call.respond("Произошла ошибка: ${e.message}")
            }
        }

        post {
            try {
                val userId = call.parameters["id"]!!.toInt()
                val departmentIds = call.receive<List<Int>>()

                database.useTransaction {
                    departmentIds.map { departmentId ->
                        database.insert(UserDepartments) {
                            set(it.userId, userId)
                            set(it.departmentId, departmentId)
                        }
                    }
                }

                call.respond(HttpStatusCode.Created)
            } catch (e: Exception) {
                println("Произошла ошибка: ${e.message}")
                call.respond("Произошла ошибка: ${e.message}")
            }
        }

        delete {
            try {
                val userId = call.parameters["id"]!!.toInt()
                val departmentIds = call.receive<List<Int>>()

                database.useTransaction {
                    departmentIds.map { departmentId ->
                        database.delete(UserDepartments) {
                            (it.userId eq userId) and (it.departmentId eq departmentId)
                        }
                    }
                }

                call.respond(departmentIds)
            } catch (e: Exception) {
                println("Произошла ошибка: ${e.message}")
                call.respond("Произошла ошибка: ${e.message}")
            }
        }
    }
}