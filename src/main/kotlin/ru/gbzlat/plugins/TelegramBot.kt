package ru.gbzlat.plugins

import io.ktor.server.application.*
import ru.gbzlat.tgbot
import ru.gbzlat.tgbot.TelegramBotFactory

fun Application.configureTelegramBot(token: String?) {
    if (token == null) {
        log.info("tgbot.token is not set, Telegram notifications are disabled")
        return
    }

    tgbot = TelegramBotFactory.connectBot(token).also { it.startPolling() }
}
