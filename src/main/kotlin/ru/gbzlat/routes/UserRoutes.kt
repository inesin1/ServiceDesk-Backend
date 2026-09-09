package ru.gbzlat.routes

import io.github.smiley4.ktoropenapi.delete
import io.github.smiley4.ktoropenapi.get
import io.github.smiley4.ktoropenapi.post
import io.github.smiley4.ktoropenapi.put
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.utils.io.*
import kotlinx.io.readByteArray
import ru.gbzlat.db.Roles
import ru.gbzlat.dto.*
import ru.gbzlat.error.badRequest
import ru.gbzlat.security.Role
import ru.gbzlat.security.UserPrincipal
import ru.gbzlat.security.requireRole
import ru.gbzlat.service.UserService
import java.io.File

private fun ApplicationCall.wantsDepartments() =
    request.queryParameters["with"]?.split(",")?.contains("departments") == true

fun Route.userRoute() {
    route("/users") {
        get({
            operationId = "listUsers"
            summary = "Список пользователей"
            tags = listOf("Пользователи")
            request {
                queryParameter<Int>("limit") { required = false }
                queryParameter<Long>("offset") { required = false }
                queryParameter<String>("with") { description = "departments — подгрузить отделения"; required = false }
            }
            response {
                code(HttpStatusCode.OK) {
                    description = "Успешно"
                    body<Page<UserResponse>>()
                }
            }
        }) {
            call.respond(
                UserService.list(
                    limit = call.request.queryParameters["limit"]?.toInt() ?: 1000,
                    offset = call.request.queryParameters["offset"]?.toLong() ?: 0,
                    withDepartments = call.wantsDepartments(),
                )
            )
        }
        get("/current", {
            operationId = "getCurrentUser"
            summary = "Текущий пользователь"
            tags = listOf("Пользователи")
            request { queryParameter<String>("with") { required = false } }
            response {
                code(HttpStatusCode.OK) {
                    description = "Успешно"
                    body<UserResponse>()
                }
            }
        }) {
            val userId = call.principal<UserPrincipal>()!!.id
            call.respond(UserService.byId(userId, call.wantsDepartments()))
        }
        get("/specialists", {
            operationId = "listSpecialists"
            summary = "ИТ-специалисты и администраторы"
            tags = listOf("Пользователи")
            request { queryParameter<String>("with") { required = false } }
            response {
                code(HttpStatusCode.OK) {
                    description = "Успешно"
                    body<List<UserResponse>>()
                }
            }
        }) {
            call.respond(UserService.specialists(call.wantsDepartments()))
        }
        get("/checklogin/{login}", {
            operationId = "checkLogin"
            summary = "Свободен ли логин"
            tags = listOf("Пользователи")
            request { pathParameter<String>("login") }
            response {
                code(HttpStatusCode.OK) {
                    description = "Успешно"
                    body<LoginAvailability>()
                }
            }
        }) {
            val taken = UserService.isLoginTaken(call.parameters["login"]!!)
            call.respond(LoginAvailability(available = !taken))
        }
        requireRole(Role.ADMIN) {
            post({
                operationId = "createUser"
                summary = "Создать пользователя"
                tags = listOf("Пользователи")
                request { body<UserDTO>() }
                response {
                    code(HttpStatusCode.Created) {
                        description = "Создано"
                        body<UserResponse>()
                    }
                    code(HttpStatusCode.Forbidden) {
                        description = "Недостаточно прав"
                        description = "Только для администратора"
                    }
                }
            }) {
                call.respond(HttpStatusCode.Created, UserService.create(call.receive<UserDTO>()))
            }
            post("/upload", {
                operationId = "importUsers"
                summary = "Импорт пользователей из файла"
                description = "TSV: ФИО, логин, пароль, id роли, —, телефон, Telegram ChatId."
                tags = listOf("Пользователи")
                request {
                    queryParameter<Boolean>("rewrite") {
                        description = "Очистить таблицу перед импортом, сделав резервную копию"
                        required = false
                    }
                    multipartBody { part<ByteArray>("file") }
                }
                response {
                    code(HttpStatusCode.Created) {
                        description = "Создано"
                        body<ImportResult>()
                    }
                    code(HttpStatusCode.BadRequest) {
                        description = "Некорректный запрос"
                        body<ErrorResponse>()
                    }
                }
            }) {
                val rewrite = call.request.queryParameters["rewrite"] == "true"
                val file = receiveUpload() ?: badRequest("Файл не приложен")

                call.respond(HttpStatusCode.Created, ImportResult(UserService.importFrom(file, rewrite)))
            }
        }
        route("/{id}") {
            get({
                operationId = "getUser"
                summary = "Пользователь по id"
                tags = listOf("Пользователи")
                request {
                    pathParameter<Int>("id")
                    queryParameter<String>("with") { required = false }
                }
                response {
                    code(HttpStatusCode.OK) {
                        description = "Успешно"
                        body<UserResponse>()
                    }
                    code(HttpStatusCode.NotFound) {
                        description = "Не найдено"
                        body<ErrorResponse>()
                    }
                }
            }) {
                call.respond(UserService.byId(call.parameters["id"]!!.toInt(), call.wantsDepartments()))
            }
            put({
                operationId = "updateUser"
                summary = "Изменить пользователя"
                description = "Своё — сам, чужое — только администратор. Роль и отделения меняет только администратор."
                tags = listOf("Пользователи")
                request {
                    pathParameter<Int>("id")
                    body<UserDTO>()
                }
                response {
                    code(HttpStatusCode.OK) {
                        description = "Успешно"
                        body<UserResponse>()
                    }
                    code(HttpStatusCode.Forbidden) {
                        description = "Недостаточно прав"
                        description = "Чужой профиль"
                    }
                    code(HttpStatusCode.NotFound) {
                        description = "Не найдено"
                        body<ErrorResponse>()
                    }
                }
            }) {
                val id = call.parameters["id"]!!.toInt()
                val principal = call.principal<UserPrincipal>()!!
                val isAdmin = principal.role == Role.ADMIN
                if (!isAdmin && principal.id != id) return@put call.respond(HttpStatusCode.Forbidden)

                call.respond(UserService.update(id, call.receive<UserDTO>(), isAdmin))
            }
            delete({
                operationId = "deleteUser"
                summary = "Удалить пользователя"
                tags = listOf("Пользователи")
                request { pathParameter<Int>("id") }
                response {
                    code(HttpStatusCode.NoContent) {
                        description = "Выполнено"
                        description = "Пользователь удалён"
                    }
                    code(HttpStatusCode.Forbidden) {
                        description = "Недостаточно прав"
                        description = "Только для администратора"
                    }
                    code(HttpStatusCode.NotFound) {
                        description = "Не найдено"
                        body<ErrorResponse>()
                    }
                }
            }) {
                if (call.principal<UserPrincipal>()!!.role != Role.ADMIN) {
                    return@delete call.respond(HttpStatusCode.Forbidden)
                }

                UserService.delete(call.parameters["id"]!!.toInt())
                call.respond(HttpStatusCode.NoContent)
            }
            userDepartmentsRoute()
        }

        refRoutes("/roles", Roles, "Роли", "Role", "Roles")
    }
}

private suspend fun RoutingContext.receiveUpload(): File? {
    var file: File? = null

    call.receiveMultipart().forEachPart { part ->
        if (part is PartData.FileItem) {
            File("uploads").mkdirs()
            file = File("uploads/users_${System.currentTimeMillis()}.tsv")
                .apply { writeBytes(part.provider().readRemaining().readByteArray()) }
        }
        part.release()
    }

    return file
}

// /users/{id}/departments
fun Route.userDepartmentsRoute() {
    route("/departments") {
        get({
            operationId = "listUserDepartments"
            summary = "Отделения пользователя"
            tags = listOf("Пользователи")
            request { pathParameter<Int>("id") }
            response {
                code(HttpStatusCode.OK) {
                    description = "Успешно"
                    body<List<Ref>>()
                }
            }
        }) {
            call.respond(UserService.departmentsOf(call.parameters["id"]!!.toInt()))
        }
        post({
            operationId = "addUserDepartments"
            summary = "Добавить отделения"
            tags = listOf("Пользователи")
            request {
                pathParameter<Int>("id")
                body<List<Int>>()
            }
            response {
                code(HttpStatusCode.Created) {
                    description = "Создано"
                    description = "Отделения добавлены"
                }
            }
        }) {
            UserService.addDepartments(call.parameters["id"]!!.toInt(), call.receive<List<Int>>())
            call.respond(HttpStatusCode.Created)
        }
        delete({
            operationId = "removeUserDepartments"
            summary = "Убрать отделения"
            tags = listOf("Пользователи")
            request {
                pathParameter<Int>("id")
                body<List<Int>>()
            }
            response {
                code(HttpStatusCode.NoContent) {
                    description = "Выполнено"
                    description = "Отделения убраны"
                }
            }
        }) {
            UserService.removeDepartments(call.parameters["id"]!!.toInt(), call.receive<List<Int>>())
            call.respond(HttpStatusCode.NoContent)
        }
    }
}
