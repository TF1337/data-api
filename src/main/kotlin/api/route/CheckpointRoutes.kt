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
import nl.freek.vgwm.domain.repository.CheckpointRepository

/**
 * Registers read-only checkpoint HTTP routes.
 */
fun Route.checkpointRoutes(checkpointRepository: CheckpointRepository) {
    authenticate("auth-jwt") {
        route("/api/checkpoints") {
            get {
                val locationId = call.request.queryParameters["locationId"]
                if (locationId != null && locationId.isBlank()) {
                    call.respond(
                        status = HttpStatusCode.BadRequest,
                        message = ErrorResponseDto(
                            message = "Query parameter 'locationId' must not be blank.",
                            code = "INVALID_LOCATION_ID",
                        ),
                    )
                    return@get
                }

                val checkpoints = if (locationId == null) {
                    checkpointRepository.findAll()
                } else {
                    checkpointRepository.findByLocation(LocationId(locationId))
                }

                call.respond(checkpoints.map { it.toDto() })
            }
        }
    }
}
