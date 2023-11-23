package ru.gbzlat.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.ktorm.dsl.delete
import org.ktorm.dsl.eq
import org.ktorm.entity.add
import org.ktorm.entity.find
import org.ktorm.entity.toList
import ru.gbzlat.database
import ru.gbzlat.database.models.Department
import ru.gbzlat.database.models.Departments
import ru.gbzlat.database.models.Departments.departments
import ru.gbzlat.dto.DepartmentDTO
import ru.gbzlat.plugins.objectMapper

fun Route.departmentRoute() {
    route("/departments") {
        get {
            try {
                call.respond(database.departments.toList())
            } catch (e: Exception) {
                println("Произошла ошибка: ${e.message}")
                call.respond("Произошла ошибка: ${e.message}")
            }
        }
        get ("/{id}") {
            try {
                val id = call.parameters["id"]!!.toInt()
                val department = database.departments.find { it.id eq id }

                if (department == null) {
                    call.respond(HttpStatusCode.NotFound)
                }

                call.respond(
                    objectMapper.writeValueAsString(
                        department!!
                    )
                )
            } catch (e: Exception) {
                println("Произошла ошибка: ${e.message}")
                call.respond("Произошла ошибка: ${e.message}")
            }
        }
        post {
            try {
                val departmentData = call.receive<DepartmentDTO>()

                database.departments.add(
                    Department {
                        name = departmentData.name
                    }
                )

                call.respond(HttpStatusCode.Created)
            } catch (e: Exception){
                println("Произошла ошибка: ${e.message}")
                call.respond("Произошла ошибка: ${e.message}")
            }
        }
        delete("/{id}") {
            try {
                val id = call.parameters["id"]!!.toInt()

                database.delete(Departments) {
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

/*fun Route.divisionRoute() {
    route("/divisions"){
        get {
            call.respond(ru.gbzlat.database.divisions.toList())
        }
        get("/{id}") {
            call.respond(
                gson.toJson(
                    ru.gbzlat.database.divisions.find {
                        it.id eq call.parameters["id"]!!.toInt()
                    }
                )
            )
        }
        post {
            try {
                val division = call.receive<EnumPojo>()

                ru.gbzlat.database.database.insert(Divisions) {
                    set(it.name, division.name)
                }

                call.respond(HttpStatusCode.OK);
            } catch (e: Exception){
                call.respond(HttpStatusCode.NotAcceptable)
            }
        }
        delete("/{id}") {
            ru.gbzlat.database.database.delete(Divisions) { it.id eq call.parameters["id"]!!.toInt() }
            call.respond(HttpStatusCode.OK)
        }
    }
}*/
