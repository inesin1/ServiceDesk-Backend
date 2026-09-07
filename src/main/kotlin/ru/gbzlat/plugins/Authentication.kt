package ru.gbzlat.plugins

import io.ktor.server.application.*
import io.ktor.server.auth.jwt.*
import ru.gbzlat.authentication.Authentication
import ru.gbzlat.authentication.Role
import ru.gbzlat.authentication.UserPrincipal

fun Application.configureAuthentication() {
    val jwt = environment.config.config("jwt")
    Authentication.initialize(
        secret = jwt.property("secret").getString(),
        issuer = jwt.property("issuer").getString(),
        audience = jwt.property("audience").getString(),
        ttlHours = jwt.property("ttlHours").getString().toLong(),
    )

    install(io.ktor.server.auth.Authentication) {
        jwt("auth-jwt") {
            verifier(Authentication.instance.verifier)
            validate { credential ->
                val id = credential.payload.getClaim(Authentication.CLAIM_ID).asInt()
                val role = credential.payload.getClaim(Authentication.CLAIM_ROLE).asInt()?.let(Role::byId)
                if (id != null && role != null) UserPrincipal(id, role) else null
            }
        }
    }
}
