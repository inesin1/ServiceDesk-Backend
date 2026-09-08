package ru.gbzlat.routes

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
import ru.gbzlat.dto.UserDTO
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
        get {
            call.respond(
                UserService.list(
                    limit = call.request.queryParameters["limit"]?.toInt() ?: 1000,
                    offset = call.request.queryParameters["offset"]?.toLong() ?: 0,
                    withDepartments = call.wantsDepartments(),
                )
            )
        }
        get("/current") {
            val userId = call.principal<UserPrincipal>()!!.id
            call.respond(UserService.byId(userId, call.wantsDepartments()))
        }
        get("/specialists") {
            call.respond(UserService.specialists(call.wantsDepartments()))
        }
        get("/checklogin/{login}") {
            val taken = UserService.isLoginTaken(call.parameters["login"]!!)
            call.respond(mapOf("available" to !taken))
        }
        requireRole(Role.ADMIN) {
            post {
                call.respond(HttpStatusCode.Created, UserService.create(call.receive<UserDTO>()))
            }
            post("/upload") {
                val rewrite = call.request.queryParameters["rewrite"] == "true"
                val file = receiveUpload() ?: badRequest("Файл не приложен")

                call.respond(
                    HttpStatusCode.Created,
                    mapOf("imported" to UserService.importFrom(file, rewrite)),
                )
            }
        }
        route("/{id}") {
            get {
                call.respond(UserService.byId(call.parameters["id"]!!.toInt(), call.wantsDepartments()))
            }
            put {
                val id = call.parameters["id"]!!.toInt()
                val principal = call.principal<UserPrincipal>()!!
                val isAdmin = principal.role == Role.ADMIN
                if (!isAdmin && principal.id != id) return@put call.respond(HttpStatusCode.Forbidden)

                call.respond(UserService.update(id, call.receive<UserDTO>(), isAdmin))
            }
            delete {
                if (call.principal<UserPrincipal>()!!.role != Role.ADMIN) {
                    return@delete call.respond(HttpStatusCode.Forbidden)
                }

                UserService.delete(call.parameters["id"]!!.toInt())
                call.respond(HttpStatusCode.NoContent)
            }
            userDepartmentsRoute()
        }

        refRoutes("/roles", Roles)
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
        get {
            call.respond(UserService.departmentsOf(call.parameters["id"]!!.toInt()))
        }
        post {
            UserService.addDepartments(call.parameters["id"]!!.toInt(), call.receive<List<Int>>())
            call.respond(HttpStatusCode.Created)
        }
        delete {
            UserService.removeDepartments(call.parameters["id"]!!.toInt(), call.receive<List<Int>>())
            call.respond(HttpStatusCode.NoContent)
        }
    }
}
