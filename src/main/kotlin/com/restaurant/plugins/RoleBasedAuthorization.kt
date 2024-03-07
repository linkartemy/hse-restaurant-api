package com.restaurant.plugins

import com.restaurant.schemas.UserRole
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*

class PluginConfiguration {
    var roles: Set<UserRole> = UserRole.entries.toSet()
}

val RoleBasedAuthorizationPlugin = createRouteScopedPlugin(
    name = "RbacPlugin",
    createConfiguration = ::PluginConfiguration
) {
    val roles = pluginConfig.roles

    pluginConfig.apply {

        on(AuthenticationChecked) { call ->
            val tokenRole = getRoleFromToken(call)

            val authorized = roles.contains(tokenRole)

            if (!authorized) {
                println("User does not have any of the following roles: $roles")
                call.respond(HttpStatusCode.Forbidden, "User does not have any of the following roles: $roles")
            }
        }
    }
}

private fun getRoleFromToken(call: ApplicationCall): UserRole? =
    UserRole.entries.firstOrNull {
        it.name == call.principal<JWTPrincipal>()
            ?.payload
            ?.getClaim("role")
            ?.asString()
    }