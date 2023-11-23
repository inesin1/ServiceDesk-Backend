package ru.gbzlat

import com.github.kotlintelegrambot.Bot
import io.ktor.server.application.*
import io.ktor.server.netty.*
import org.ktorm.database.Database
import ru.gbzlat.plugins.configureDatabase
import ru.gbzlat.plugins.*

lateinit var database: Database
lateinit var tgbot: Bot

fun main(args: Array<String>): Unit = EngineMain.main(args)

fun Application.module() {
    configureDatabase(environment)
    configureCors()
    configureAuthentication()
    configureRouting()
    configureDefaultHeaders()
    configureSerialization()
    configureTelegramBot(environment)
}