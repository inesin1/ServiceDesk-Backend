package ru.gbzlat.error

import io.ktor.http.*

class ApiException(val status: HttpStatusCode, override val message: String) : RuntimeException(message)

fun badRequest(message: String): Nothing = throw ApiException(HttpStatusCode.BadRequest, message)

fun notFound(message: String): Nothing = throw ApiException(HttpStatusCode.NotFound, message)
