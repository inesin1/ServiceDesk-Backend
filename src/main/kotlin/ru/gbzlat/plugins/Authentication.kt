package ru.gbzlat.plugins

import io.ktor.server.application.*
import io.ktor.server.auth.jwt.*
import ru.gbzlat.config.AppConfig
import ru.gbzlat.security.Authentication
import ru.gbzlat.security.Role
import ru.gbzlat.security.UserPrincipal

fun Application.configureAuthentication(config: AppConfig.Jwt) {
    Authentication.initialize(config.secret, config.issuer, config.audience, config.ttlHours)

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
