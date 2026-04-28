package nl.freek.vgwm.api.route

import io.ktor.server.auth.authenticate
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import nl.freek.vgwm.api.dto.mapper.toDto
import nl.freek.vgwm.domain.repository.CategoryRepository

/**
 * Registers read-only category HTTP routes.
 */
fun Route.categoryRoutes(categoryRepository: CategoryRepository) {
    authenticate("auth-jwt") {
        route("/api/categories") {
            get {
                val categories = categoryRepository.findAll().map { it.toDto() }
                call.respond(categories)
            }
        }
    }
}
