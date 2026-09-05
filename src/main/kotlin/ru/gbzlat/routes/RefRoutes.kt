package ru.gbzlat.routes

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
import ru.gbzlat.dto.Ref
import ru.gbzlat.dto.SimpleData

/**
 * CRUD over a reference table. Statuses, roles, categories, sources and departments
 * are all the same shape, so they share one implementation.
 */
fun Route.refRoutes(path: String, table: RefTable) {
    route(path) {
        get {
            call.respond(transaction { table.allRefs() })
        }
        get("/{id}") {
            val id = call.parameters["id"]!!.toInt()
            val ref = transaction { table.findRef(id) }
                ?: return@get call.respond(HttpStatusCode.NotFound)

            call.respond(ref)
        }
        post {
            val body = call.receive<SimpleData>()
            val created = transaction {
                val newId = table.insert { it[name] = body.name } get table.id
                Ref(newId, body.name)
            }

            call.respond(HttpStatusCode.Created, created)
        }
        put("/{id}") {
            val id = call.parameters["id"]!!.toInt()
            val body = call.receive<SimpleData>()
            val updated = transaction { table.update({ table.id eq id }) { it[name] = body.name } }

            if (updated == 0) return@put call.respond(HttpStatusCode.NotFound)
            call.respond(Ref(id, body.name))
        }
        delete("/{id}") {
            val id = call.parameters["id"]!!.toInt()
            val deleted = transaction { table.deleteWhere { table.id eq id } }

            if (deleted == 0) return@delete call.respond(HttpStatusCode.NotFound)
            call.respond(HttpStatusCode.NoContent)
        }
    }
}
