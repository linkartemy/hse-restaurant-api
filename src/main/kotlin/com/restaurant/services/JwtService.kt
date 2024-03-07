package com.restaurant.services

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.restaurant.schemas.ExposedUser
import com.restaurant.schemas.UserService
import io.ktor.server.application.*
import io.ktor.server.auth.jwt.*
import java.util.*

class JwtService(private val application: Application, private val userService: UserService) {
    private val audience = application.environment.config.property("jwt.audience").getString()
    private val domain = application.environment.config.property("jwt.domain").getString()
    val realm = application.environment.config.property("jwt.realm").getString()
    private val secret = application.environment.config.property("jwt.secret").getString()

    val verifier: JWTVerifier = JWT
        .require(Algorithm.HMAC256(secret))
        .withAudience(audience)
        .withIssuer(domain)
        .build()

    private fun getConfigProperty(path: String) = application.environment.config.property(path).getString()
    private fun extractLogin(credential: JWTCredential): String? = credential.payload.getClaim("login").asString()
    private fun audienceMatches(credential: JWTCredential): Boolean = credential.payload.audience.contains(audience)

    fun createJwtToken(user: ExposedUser): String {
        return JWT.create()
            .withAudience(audience)
            .withIssuer(domain)
            .withClaim("login", user.login)
            .withClaim("role", user.role.name)
            .withExpiresAt(
                Date(System.currentTimeMillis() + 1 * 60 * 60 * 1000) // expires in 1 hour
            ).sign(Algorithm.HMAC256(secret))
    }

    suspend fun validate(credential: JWTCredential): JWTPrincipal? {
        val login = extractLogin(credential)
        val user = userService.readByLogin(login!!) ?: return null
        if (audienceMatches(credential)) {
            return JWTPrincipal(credential.payload)
        }
        return null
    }
}
