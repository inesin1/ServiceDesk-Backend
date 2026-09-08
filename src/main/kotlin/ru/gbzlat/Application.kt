package ru.gbzlat

import com.github.kotlintelegrambot.Bot
import io.ktor.server.application.*
import io.ktor.server.netty.*
import ru.gbzlat.config.AppConfig
import ru.gbzlat.db.connectDatabase
import ru.gbzlat.plugins.*

var tgbot: Bot? = null

fun main(args: Array<String>): Unit = EngineMain.main(args)

fun Application.module() {
    val config = AppConfig(environment.config)

    connectDatabase(config.database)
    configureStatusPages()
    configureCors(config.corsAllowedHosts)
    configureAuthentication(config.jwt)
    configureRouting()
    configureDefaultHeaders()
    configureSerialization()
    configureTelegramBot(config.telegramToken)
}
