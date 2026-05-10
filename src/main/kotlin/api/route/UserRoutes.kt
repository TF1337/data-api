package nl.freek.vgwm.api.route

import io.ktor.server.auth.authenticate
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import nl.freek.vgwm.api.dto.mapper.toDto
import nl.freek.vgwm.domain.repository.UserRepository

/**
 * Registers read-only user HTTP routes.
 */
fun Route.userRoutes(userRepository: UserRepository) {
    authenticate("auth-jwt") {
        route("/api/users") {
            get {
                val users = userRepository.findAll().map { it.toDto() }
                call.respond(users)
            }
        }
    }
}
