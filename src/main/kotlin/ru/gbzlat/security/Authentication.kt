package ru.gbzlat.security

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import java.time.Instant
import java.time.temporal.ChronoUnit

class Authentication private constructor(
    secret: String,
    private val issuer: String,
    private val audience: String,
    private val ttlHours: Long,
) {
    private val algorithm = Algorithm.HMAC256(secret)

    val verifier: JWTVerifier = JWT
        .require(algorithm)
        .withIssuer(issuer)
        .withAudience(audience)
        .build()

    fun createAccessToken(id: Int, role: Role): String = JWT
        .create()
        .withIssuer(issuer)
        .withAudience(audience)
        .withClaim(CLAIM_ID, id)
        .withClaim(CLAIM_ROLE, role.id)
        .withExpiresAt(Instant.now().plus(ttlHours, ChronoUnit.HOURS))
        .sign(algorithm)

    companion object {
        const val CLAIM_ID = "id"
        const val CLAIM_ROLE = "role"

        lateinit var instance: Authentication
            private set

        fun initialize(secret: String, issuer: String, audience: String, ttlHours: Long) {
            synchronized(this) {
                if (!this::instance.isInitialized) {
                    instance = Authentication(secret, issuer, audience, ttlHours)
                }
            }
        }
    }
}
