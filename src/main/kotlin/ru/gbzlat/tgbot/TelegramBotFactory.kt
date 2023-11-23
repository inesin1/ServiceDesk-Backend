package ru.gbzlat.tgbot

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.command
import com.github.kotlintelegrambot.dispatcher.text
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.KeyboardReplyMarkup
import ru.gbzlat.plugins.generateUsersButton

class TelegramBotFactory {
    companion object {
        fun connectBot(token: String): Bot {
            return bot {
                this.token = token
                dispatch {
                    command("start") {
                        val keyboardMarkup = KeyboardReplyMarkup(keyboard = generateUsersButton(), resizeKeyboard = true)
                        bot.sendMessage(
                            chatId = ChatId.fromId(message.chat.id),
                            text = "Привет, я бот Сервис Деск, буду оповещать вас о создании заявок!",
                            replyMarkup = keyboardMarkup,
                        )
                    }
                    text("Ping") {
                        bot.sendMessage(ChatId.fromId(message.chat.id), text = "Pong")
                    }
                    text("Мой ChatId") {
                        bot.sendMessage(ChatId.fromId(message.chat.id), text = "Ваш ChatId: ${message.chat.id}")
                    }
                    text("Настройка бота") {
                        bot.sendMessage(
                            ChatId.fromId(message.chat.id),
                            text = """
                        1. Запросите свой ChatId (С помощью кнопки в меню, либо /chat_id)
                        2. Внесите его в свой профиль в приложении Сервис Деск
                        3. ???
                        4. Profit
                    """.trimIndent(),

                            )
                    }
                    command("chat_id") {
                        bot.sendMessage(ChatId.fromId(message.chat.id), text = "Ваш ChatId: ${message.chat.id}")
                    }
                    command("setting_up") {
                        bot.sendMessage(
                            ChatId.fromId(message.chat.id),
                            text = """
                        1. Запросите свой ChatId (С помощью кнопки в меню, либо /chat_id)
                        2. Внесите его в свой профиль в приложении Сервис Деск
                        3. ???
                        4. Profit
                    """.trimIndent(),

                            )
                    }
                }
            }
        }
    }
}