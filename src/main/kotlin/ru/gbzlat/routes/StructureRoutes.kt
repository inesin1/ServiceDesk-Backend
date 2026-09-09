package ru.gbzlat.routes

import io.ktor.server.routing.*
import ru.gbzlat.db.Departments

fun Route.departmentRoute() = refRoutes("/departments", Departments, "Отделения", "Department", "Departments")
