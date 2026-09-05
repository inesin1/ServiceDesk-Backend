package ru.gbzlat.plugins

import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import io.ktor.serialization.jackson.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*

fun Application.configureSerialization() {
    install(ContentNegotiation) {
        jackson {
            registerModule(JavaTimeModule())
            // ISO-8601 strings, not the numeric arrays Jackson defaults to for java.time.
            disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        }
    }
}
