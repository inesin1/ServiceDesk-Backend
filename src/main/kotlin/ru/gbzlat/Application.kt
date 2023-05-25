 package ru.gbzlat

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import ru.gbzlat.database.DatabaseManager
import ru.gbzlat.plugins.*

 val db = DatabaseManager()

/*fun main() {
    *//*embeddedServer(Netty, port = 0102, host = "0.0.0.0", module = Application::module)
        .start(wait = true)*//*

}*/
fun main(args: Array<String>): Unit = EngineMain.main(args)

fun Application.module() {
    configureCors()
    configureSessions()
    configureAuthentication()
    configureRouting()
    configureSerialization()
}
