package nl.freek.vgwm.api.route

import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import nl.freek.vgwm.api.dto.ErrorResponseDto
import nl.freek.vgwm.api.dto.mapper.toDto
import nl.freek.vgwm.domain.id.LocationId
import nl.freek.vgwm.domain.repository.LocationRepository

/**
 * Registers read-only location HTTP routes.
 */
fun Route.locationRoutes(locationRepository: LocationRepository) {
    authenticate("auth-jwt") {
        route("/api/locations") {
            get {
                val locations = locationRepository.findAll().map { it.toDto() }
                call.respond(locations)
            }

            get("/{id}/children") {
                val locationId = call.parameters["id"]
                if (locationId.isNullOrBlank()) {
                    call.respond(
                        status = HttpStatusCode.BadRequest,
                        message = ErrorResponseDto(
                            message = "Path parameter 'id' is required.",
                            code = "INVALID_LOCATION_ID",
                        ),
                    )
                    return@get
                }

                val children = locationRepository.findChildren(LocationId(locationId)).map { it.toDto() }
                call.respond(children)
            }
        }
    }
}
