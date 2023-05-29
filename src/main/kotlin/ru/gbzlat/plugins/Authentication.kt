package ru.gbzlat.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.application.*
import io.ktor.server.auth.jwt.*

class Authentication private constructor(secret: String){
    private val algorithm = Algorithm.HMAC256(secret)

    val verifier: JWTVerifier = JWT
        .require(algorithm)
        .withIssuer(ISSUER)
        .withAudience(AUDIENCE)
        .build()

    fun createAccessToken(id: Int): String = JWT
        .create()
        .withIssuer(ISSUER)
        .withAudience(AUDIENCE)
        .withClaim(CLAIM, id)
        .sign(algorithm)

    companion object {
        private const val ISSUER = "http://10.9.5.127:1002"
        private const val AUDIENCE = "http://10.9.5.127:1002/hello"
        const val CLAIM = "id"

        lateinit var instance: Authentication
            private set

        fun initialize(secret: String) {
            synchronized(this) {
                if (!this::instance.isInitialized) {
                    instance = Authentication(secret)
                }
            }
        }
    }
}

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