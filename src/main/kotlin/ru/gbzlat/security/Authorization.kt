package ru.gbzlat.security

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.application.hooks.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

class RoleAuthorizationConfig {
    var allowed: Set<Role> = emptySet()
}

private val RoleAuthorization = createRouteScopedPlugin("RoleAuthorization", ::RoleAuthorizationConfig) {
    val allowed = pluginConfig.allowed

    on(AuthenticationChecked) { call ->
        val role = call.principal<UserPrincipal>()?.role
        if (role == null || role !in allowed) {
            call.respond(HttpStatusCode.Forbidden)
        }
    }
}

/** Restricts everything built inside to the given roles. */
fun Route.requireRole(vararg roles: Role, build: Route.() -> Unit) {
    val child = createChild(TransparentRouteSelector)
    child.install(RoleAuthorization) { allowed = roles.toSet() }
    child.build()
}

private object TransparentRouteSelector : RouteSelector() {
    override suspend fun evaluate(context: RoutingResolveContext, segmentIndex: Int) =
        RouteSelectorEvaluation.Transparent

    override fun toString() = "(role)"
}
