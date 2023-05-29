package ru.gbzlat.plugins

import io.ktor.server.auth.*

data class UserPrincipal(
    val id: Int
) : Principal
