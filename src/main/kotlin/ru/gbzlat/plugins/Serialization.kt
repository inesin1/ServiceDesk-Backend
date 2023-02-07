package ru.gbzlat.plugins

import com.google.gson.GsonBuilder
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.util.*
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import java.time.LocalDateTime

val gson = GsonBuilder().create()

fun Application.configureSerialization() {
    install(ContentNegotiation) {
        json()
    }
}

object LocalDateTimeSerializer : KSerializer<LocalDateTime> {
    override fun deserialize(decoder: Decoder): LocalDateTime =
        LocalDateTime.parse(decoder.decodeString())

    override fun serialize(encoder: kotlinx.serialization.encoding.Encoder, value: LocalDateTime) {

    }

    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("LocalDateTime", PrimitiveKind.STRING)
}