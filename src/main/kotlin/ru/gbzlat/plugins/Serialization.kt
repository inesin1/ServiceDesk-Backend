package ru.gbzlat.plugins

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import io.ktor.serialization.jackson.*

val objectMapper = ObjectMapper()
fun Application.configureSerialization() {
    install(ContentNegotiation) {
        jackson()
    }

    objectMapper.registerModule(JavaTimeModule())
    objectMapper.findAndRegisterModules()
    objectMapper.setDateFormat(SimpleDateFormat("yyyy-MM-dd HH:mm a z"))
}

object LocalDateTimeSerializer : KSerializer<LocalDateTime> {
    override fun deserialize(decoder: Decoder): LocalDateTime =
        LocalDateTime.parse(decoder.decodeString())

    override fun serialize(encoder: kotlinx.serialization.encoding.Encoder, value: LocalDateTime) {

    }

    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("LocalDateTime", PrimitiveKind.STRING)
}