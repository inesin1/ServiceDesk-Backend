package ru.gbzlat.routes

import io.github.smiley4.ktoropenapi.delete
import io.github.smiley4.ktoropenapi.get
import io.github.smiley4.ktoropenapi.post
import io.github.smiley4.ktoropenapi.put
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update
import ru.gbzlat.db.RefTable
import ru.gbzlat.db.allRefs
import ru.gbzlat.db.findRef
import ru.gbzlat.dto.ErrorResponse
import ru.gbzlat.dto.Ref
import ru.gbzlat.dto.SimpleData
import ru.gbzlat.error.notFound
import ru.gbzlat.security.Role
import ru.gbzlat.security.requireRole

/**
 * CRUD over a reference table. Statuses, roles, categories, sources and departments
 * are all the same shape, so they share one implementation. Everyone reads them,
 * only admins change them.
 */
fun Route.refRoutes(path: String, table: RefTable, tag: String, one: String, many: String) {
    route(path) {
        get({
            operationId = "list$many"
            summary = "Список"
            tags = listOf(tag)
            response {
                code(HttpStatusCode.OK) {
                    description = "Успешно"
                    body<List<Ref>>()
                }
            }
        }) {
            call.respond(transaction { table.allRefs() })
        }
        get("/{id}", {
            operationId = "get$one"
            summary = "Запись по id"
            tags = listOf(tag)
            request { pathParameter<Int>("id") }
            response {
                code(HttpStatusCode.OK) {
                    description = "Успешно"
                    body<Ref>()
                }
                code(HttpStatusCode.NotFound) {
                    description = "Не найдено"
                    body<ErrorResponse>()
                }
            }
        }) {
            val id = call.parameters["id"]!!.toInt()
            call.respond(transaction { table.findRef(id) } ?: notFound("Запись №$id не найдена"))
        }

        requireRole(Role.ADMIN) {
            post({
                operationId = "create$one"
                summary = "Создать запись"
                tags = listOf(tag)
                request { body<SimpleData>() }
                response {
                    code(HttpStatusCode.Created) {
                        description = "Создано"
                        body<Ref>()
                    }
                    code(HttpStatusCode.Forbidden) {
                        description = "Недостаточно прав"
                        description = "Только для администратора"
                    }
                }
            }) {
                val body = call.receive<SimpleData>()
                val created = transaction {
                    val newId = table.insert { it[name] = body.name } get table.id
                    Ref(newId, body.name)
                }

                call.respond(HttpStatusCode.Created, created)
            }
            put("/{id}", {
                operationId = "update$one"
                summary = "Переименовать запись"
                tags = listOf(tag)
                request {
                    pathParameter<Int>("id")
                    body<SimpleData>()
                }
                response {
                    code(HttpStatusCode.OK) {
                        description = "Успешно"
                        body<Ref>()
                    }
                    code(HttpStatusCode.NotFound) {
                        description = "Не найдено"
                        body<ErrorResponse>()
                    }
                    code(HttpStatusCode.Forbidden) {
                        description = "Недостаточно прав"
                        description = "Только для администратора"
                    }
                }
            }) {
                val id = call.parameters["id"]!!.toInt()
                val body = call.receive<SimpleData>()
                val updated = transaction { table.update({ table.id eq id }) { it[name] = body.name } }

                if (updated == 0) notFound("Запись №$id не найдена")
                call.respond(Ref(id, body.name))
            }
            delete("/{id}", {
                operationId = "delete$one"
                summary = "Удалить запись"
                tags = listOf(tag)
                request { pathParameter<Int>("id") }
                response {
                    code(HttpStatusCode.NoContent) {
                        description = "Выполнено"
                        description = "Запись удалена"
                    }
                    code(HttpStatusCode.NotFound) {
                        description = "Не найдено"
                        body<ErrorResponse>()
                    }
                    code(HttpStatusCode.Forbidden) {
                        description = "Недостаточно прав"
                        description = "Только для администратора"
                    }
                }
            }) {
                val id = call.parameters["id"]!!.toInt()
                val deleted = transaction { table.deleteWhere { table.id eq id } }

                if (deleted == 0) notFound("Запись №$id не найдена")
                call.respond(HttpStatusCode.NoContent)
            }
        }
    }
}
