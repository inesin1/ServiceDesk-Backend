package ru.gbzlat.plugins

import com.google.gson.GsonBuilder
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*

val gson = GsonBuilder().create()

fun Application.configureSerialization() {
    install(ContentNegotiation) {
        json()
    }
}