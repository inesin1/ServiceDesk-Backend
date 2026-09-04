package ru.gbzlat.plugins

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import io.ktor.serialization.jackson.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import java.text.SimpleDateFormat

val objectMapper = ObjectMapper()

fun Application.configureSerialization() {
    install(ContentNegotiation) {
        jackson()
    }

    objectMapper.registerModule(JavaTimeModule())
    objectMapper.findAndRegisterModules()
    objectMapper.setDateFormat(SimpleDateFormat("yyyy-MM-dd HH:mm a z"))
}
