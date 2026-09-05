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
import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.jdbc.*
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import ru.gbzlat.authentication.UserPrincipal
import ru.gbzlat.db.*
import ru.gbzlat.dto.UserDTO
import java.io.File
import java.time.LocalDateTime

private const val ROLE_EMPLOYEE = 1

private fun ApplicationCall.wantsDepartments() =
    request.queryParameters["with"]?.split(",")?.contains("departments") == true

fun Route.userRoute() {
    route("/users") {
        get {
            val withDepartments = call.wantsDepartments()
            val offset = call.request.queryParameters["offset"]?.toLong() ?: 0
            val limit = call.request.queryParameters["limit"]?.toInt() ?: 1000

            val users = transaction {
                val ids = Users.select(Users.id).orderBy(Users.id).limit(limit).offset(offset)
                    .map { it[Users.id] }
                loadUsers(ids, withDepartments).values.sortedBy { it.id }
            }

            call.respond(users)
        }
        get("/current") {
            val userId = call.principal<UserPrincipal>()!!.id
            val user = transaction { loadUsers(listOf(userId), call.wantsDepartments())[userId] }
                ?: return@get call.respond(HttpStatusCode.NotFound)

            call.respond(user)
        }
        get("/specialists") {
            val withDepartments = call.wantsDepartments()
            val users = transaction {
                val ids = Users.select(Users.id).where { Users.roleId neq ROLE_EMPLOYEE }
                    .map { it[Users.id] }
                loadUsers(ids, withDepartments).values.sortedBy { it.id }
            }

            call.respond(users)
        }
        get("/checklogin/{login}") {
            val login = call.parameters["login"]!!
            val taken = transaction { Users.selectAll().where { Users.login eq login }.any() }

            call.respond(mapOf("available" to !taken))
        }
        post {
            val body = call.receive<UserDTO>()

            val id = transaction {
                val newId = Users.insert {
                    it[name] = body.name
                    it[login] = body.login
                    it[password] = body.password
                    it[roleId] = body.roleId
                    it[phone] = body.phone
                    it[tgChatId] = body.tgChatId
                } get Users.id

                setDepartments(newId, body.departmentIds)
                newId
            }

            call.respond(HttpStatusCode.Created, transaction { loadUsers(listOf(id), true).getValue(id) })
        }
        post("/upload") {
            val rewrite = call.request.queryParameters["rewrite"] == "true"
            val file = receiveUpload() ?: return@post call.respond(HttpStatusCode.BadRequest)

            val imported = transaction {
                if (rewrite) {
                    File("backup").mkdirs()
                    File("backup/users_${LocalDateTime.now()}.tsv")
                        .writeText(Users.selectAll().joinToString("\n") { row ->
                            listOf(
                                row[Users.name], row[Users.login], row[Users.password],
                                row[Users.roleId], row[Users.phone].orEmpty(), row[Users.tgChatId] ?: ""
                            ).joinToString("\t")
                        })
                    UserDepartments.deleteAll()
                    Users.deleteAll()
                }

                file.readLines().count { importUserLine(it) }
            }

            call.respond(HttpStatusCode.Created, mapOf("imported" to imported))
        }
        route("/{id}") {
            get {
                val id = call.parameters["id"]!!.toInt()
                val user = transaction { loadUsers(listOf(id), call.wantsDepartments())[id] }
                    ?: return@get call.respond(HttpStatusCode.NotFound)

                call.respond(user)
            }
            put {
                val id = call.parameters["id"]!!.toInt()
                val body = call.receive<UserDTO>()

                val updated = transaction {
                    val rows = Users.update({ Users.id eq id }) {
                        it[name] = body.name
                        it[login] = body.login
                        it[password] = body.password
                        it[roleId] = body.roleId
                        it[phone] = body.phone
                        it[tgChatId] = body.tgChatId
                    }
                    if (rows > 0) setDepartments(id, body.departmentIds)
                    rows
                }

                if (updated == 0) return@put call.respond(HttpStatusCode.NotFound)
                call.respond(transaction { loadUsers(listOf(id), true).getValue(id) })
            }
            delete {
                val id = call.parameters["id"]!!.toInt()

                val deleted = transaction {
                    UserDepartments.deleteWhere { userId eq id }
                    Users.deleteWhere { Users.id eq id }
                }

                if (deleted == 0) return@delete call.respond(HttpStatusCode.NotFound)
                call.respond(HttpStatusCode.NoContent)
            }
            userDepartmentsRoute()
        }

        refRoutes("/roles", Roles)
    }
}

private fun setDepartments(userId: Int, departmentIds: List<Int>) {
    UserDepartments.deleteWhere { UserDepartments.userId eq userId }
    UserDepartments.batchInsert(departmentIds) { departmentId ->
        this[UserDepartments.userId] = userId
        this[UserDepartments.departmentId] = departmentId
    }
}

/** Tab-separated: name, login, password, roleId, (unused), phone, tgChatId. */
private fun importUserLine(line: String): Boolean {
    val cells = line.split('\t')
    if (cells.size < 7) return false

    val login = cells[1]
    val chatId = cells[6].toLongOrNull()
    val role = cells[3].toIntOrNull() ?: return false

    val existing = Users.select(Users.id).where { Users.login eq login }.singleOrNull()
    if (existing == null) {
        Users.insert {
            it[name] = cells[0]
            it[Users.login] = login
            it[password] = cells[2]
            it[roleId] = role
            it[phone] = cells[5]
            it[tgChatId] = chatId
        }
    } else {
        Users.update({ Users.login eq login }) {
            it[name] = cells[0]
            it[password] = cells[2]
            it[roleId] = role
            it[phone] = cells[5]
            it[tgChatId] = chatId
        }
    }
    return true
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
            val userId = call.parameters["id"]!!.toInt()
            call.respond(transaction { departmentsByUser(listOf(userId))[userId].orEmpty() })
        }
        post {
            val userId = call.parameters["id"]!!.toInt()
            val departmentIds = call.receive<List<Int>>()

            transaction {
                UserDepartments.batchInsert(departmentIds, ignore = true) { departmentId ->
                    this[UserDepartments.userId] = userId
                    this[UserDepartments.departmentId] = departmentId
                }
            }

            call.respond(HttpStatusCode.Created)
        }
        delete {
            val userId = call.parameters["id"]!!.toInt()
            val departmentIds = call.receive<List<Int>>()

            transaction {
                UserDepartments.deleteWhere {
                    (UserDepartments.userId eq userId) and (departmentId inList departmentIds)
                }
            }

            call.respond(HttpStatusCode.NoContent)
        }
    }
}
