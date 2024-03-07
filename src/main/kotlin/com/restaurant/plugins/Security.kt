package com.restaurant.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.restaurant.services.JwtService
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import com.restaurant.plugins.RoleBasedAuthorizationPlugin
import com.restaurant.schemas.UserRole
import io.ktor.server.application.*
import io.ktor.server.routing.*

fun Application.configureSecurity(jwtService: JwtService) {
    authentication {
        jwt {
            realm = jwtService.realm
            verifier(
                jwtService.verifier
            )
            validate { credential ->
                jwtService.validate(credential)
            }
        }
    }
}

fun Route.authorized(
    vararg hasAnyRole: UserRole,
    build: Route.() -> Unit
) {
    install(RoleBasedAuthorizationPlugin) { roles = hasAnyRole.toSet() }
    build()
}
