 package ru.gbzlat

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import ru.gbzlat.database.DatabaseManager
import ru.gbzlat.plugins.*

 val db = DatabaseManager()

fun main() {
    embeddedServer(Netty, port = 7171, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    configureCors()
    configureRouting()
    configureSerialization()
    configureAuthentication()
}
