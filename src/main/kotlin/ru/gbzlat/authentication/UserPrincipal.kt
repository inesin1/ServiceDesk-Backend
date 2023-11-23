package ru.gbzlat.authentication

import io.ktor.server.auth.*

data class UserPrincipal(
    val id: Int
) : Principal
