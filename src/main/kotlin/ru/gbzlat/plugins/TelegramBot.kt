package ru.gbzlat.plugins

import com.github.kotlintelegrambot.entities.keyboard.KeyboardButton
import io.ktor.server.application.*
import ru.gbzlat.tgbot
import ru.gbzlat.tgbot.TelegramBotFactory

fun Application.configureTelegramBot(env: ApplicationEnvironment) {
    val token = env.config.propertyOrNull("tgbot.token")?.getString()
    if (token.isNullOrBlank()) {
        log.info("tgbot.token is not set, Telegram notifications are disabled")
        return
    }

    tgbot = TelegramBotFactory.connectBot(token).also { it.startPolling() }
}

fun generateUsersButton(): List<List<KeyboardButton>> {
    return listOf(
        listOf(KeyboardButton("Мой ChatId")),
        listOf(KeyboardButton("Настройка бота")),
    )
}