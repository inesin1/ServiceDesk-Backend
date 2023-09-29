package ru.gbzlat

import com.github.kotlintelegrambot.Bot
import io.ktor.server.application.*
import io.ktor.server.netty.*
import ru.gbzlat.database.DatabaseManager
import ru.gbzlat.database.configureDatabase
import ru.gbzlat.plugins.*

lateinit var database: DatabaseManager
lateinit var tgbot: Bot

fun main(args: Array<String>): Unit = EngineMain.main(args)

fun Application.module() {
    configureDatabase(environment)
    configureCors()
    configureAuthentication()
    configureRouting()
    configureSerialization()
    configureTelegramBot()
}