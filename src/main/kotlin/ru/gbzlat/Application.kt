package ru.gbzlat

import com.github.kotlintelegrambot.Bot
import io.ktor.server.application.*
import io.ktor.server.netty.*
import ru.gbzlat.db.connectDatabase
import ru.gbzlat.plugins.*

var tgbot: Bot? = null

fun main(args: Array<String>): Unit = EngineMain.main(args)

fun Application.module() {
    connectDatabase(environment.config)
    configureCors()
    configureAuthentication()
    configureRouting()
    configureDefaultHeaders()
    configureSerialization()
    configureTelegramBot(environment)
}
