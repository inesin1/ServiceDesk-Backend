package ru.gbzlat.authentication

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm

class Authentication private constructor(secret: String, private val issuer: String, private val audience: String) {
    private val algorithm = Algorithm.HMAC256(secret)

    val verifier: JWTVerifier = JWT
        .require(algorithm)
        .withIssuer(issuer)
        .withAudience(audience)
        .build()

    fun createAccessToken(id: Int): String = JWT
        .create()
        .withIssuer(issuer)
        .withAudience(audience)
        .withClaim(CLAIM, id)
        .sign(algorithm)

    companion object {
        const val CLAIM = "id"

        lateinit var instance: Authentication
            private set

        fun initialize(secret: String, issuer: String, audience: String) {
            synchronized(this) {
                if (!this::instance.isInitialized) {
                    instance = Authentication(secret, issuer, audience)
                }
            }
        }
    }
}
