package ru.gbzlat.plugins

import io.ktor.server.application.*
import io.ktor.server.auth.jwt.*
import ru.gbzlat.authentication.Authentication
import ru.gbzlat.authentication.UserPrincipal

fun Application.configureAuthentication() {
    Authentication.initialize("bruh")
    install(io.ktor.server.auth.Authentication) {
        jwt ("auth-jwt") {
            verifier(Authentication.instance.verifier)
            validate {
                val claim = it.payload.getClaim(Authentication.CLAIM).asInt()
                if (claim != null){
                    UserPrincipal(claim)
                } else {
                    null
                }
            }
        }
    }
}