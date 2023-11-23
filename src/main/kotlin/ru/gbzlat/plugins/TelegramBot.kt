package ru.gbzlat.plugins

import com.github.kotlintelegrambot.entities.keyboard.KeyboardButton
import io.ktor.server.application.*
import ru.gbzlat.tgbot
import ru.gbzlat.tgbot.TelegramBotFactory

fun Application.configureTelegramBot(env: ApplicationEnvironment) {
    tgbot = TelegramBotFactory.connectBot(env.config.property("tgbot.token").getString())
    tgbot.startPolling()
}

fun generateUsersButton(): List<List<KeyboardButton>> {
    return listOf(
        listOf(KeyboardButton("Мой ChatId")),
        listOf(KeyboardButton("Настройка бота")),
    )
}