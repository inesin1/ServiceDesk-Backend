package ru.gbzlat.plugins

import io.ktor.server.auth.*

data class UserIdPrincipalForUser(
    val id: Int
) : Principal
