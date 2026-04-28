package nl.freek.vgwm.api.route

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.authenticate
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import nl.freek.vgwm.api.dto.ErrorResponseDto
import nl.freek.vgwm.api.dto.mapper.toDto
import nl.freek.vgwm.domain.id.TourId
import nl.freek.vgwm.domain.id.UserId
import nl.freek.vgwm.domain.repository.TourFilter
import nl.freek.vgwm.domain.repository.TourRepository
import nl.freek.vgwm.domain.value.TourStatus
import nl.freek.vgwm.domain.value.TourType

/**
 * Registers read-only tour HTTP routes.
 */
fun Route.tourRoutes(tourRepository: TourRepository) {
    authenticate("auth-jwt") {
        route("/api/tours") {
            get {
                val filter = call.buildTourFilter(
                    statusRaw = call.request.queryParameters["status"],
                    typeRaw = call.request.queryParameters["type"],
                    startedByRaw = call.request.queryParameters["startedBy"],
                    participantIdRaw = call.request.queryParameters["participantId"],
                    activeOnlyRaw = call.request.queryParameters["activeOnly"],
                )

                if (filter == null) {
                    return@get
                }

                val tours = tourRepository.findAll(filter).map { it.toDto() }
                call.respond(tours)
            }

            get("/{id}") {
                val id = call.parameters["id"]
                if (id.isNullOrBlank()) {
                    call.badRequest("Path parameter 'id' is required.", "INVALID_TOUR_ID")
                    return@get
                }

                val tour = tourRepository.findById(TourId(id))
                if (tour == null) {
                    call.notFound("Tour not found.", "TOUR_NOT_FOUND")
                    return@get
                }

                call.respond(tour.toDto())
            }
        }
    }
}

private suspend fun ApplicationCall.buildTourFilter(
    statusRaw: String?,
    typeRaw: String?,
    startedByRaw: String?,
    participantIdRaw: String?,
    activeOnlyRaw: String?,
): TourFilter? {
    val status = parseTourStatus(statusRaw)
    if (!status.isValid) return null

    val type = parseTourType(typeRaw)
    if (!type.isValid) return null

    val startedBy = parseUserId(startedByRaw, "startedBy")
    if (!startedBy.isValid) return null

    val participantId = parseUserId(participantIdRaw, "participantId")
    if (!participantId.isValid) return null

    val activeOnly = parseStrictBoolean(activeOnlyRaw)
    if (!activeOnly.isValid) return null

    return TourFilter(
        status = status.value,
        type = type.value,
        startedBy = startedBy.value,
        participantId = participantId.value,
        activeOnly = activeOnly.value ?: false,
    )
}

private suspend fun ApplicationCall.parseTourStatus(raw: String?): TourParseResult<TourStatus> {
    if (raw == null) return TourParseResult.valid(null)
    if (raw.isBlank()) {
        badRequest("Query parameter 'status' must not be blank.", "INVALID_TOUR_STATUS")
        return TourParseResult.invalid()
    }

    return try {
        TourParseResult.valid(TourStatus.valueOf(raw))
    } catch (_: IllegalArgumentException) {
        badRequest("Unknown tour status: $raw", "INVALID_TOUR_STATUS")
        TourParseResult.invalid()
    }
}

private suspend fun ApplicationCall.parseTourType(raw: String?): TourParseResult<TourType> {
    if (raw == null) return TourParseResult.valid(null)
    if (raw.isBlank()) {
        badRequest("Query parameter 'type' must not be blank.", "INVALID_TOUR_TYPE")
        return TourParseResult.invalid()
    }

    return try {
        TourParseResult.valid(TourType.valueOf(raw))
    } catch (_: IllegalArgumentException) {
        badRequest("Unknown tour type: $raw", "INVALID_TOUR_TYPE")
        TourParseResult.invalid()
    }
}

private suspend fun ApplicationCall.parseUserId(raw: String?, paramName: String): TourParseResult<UserId> {
    if (raw == null) return TourParseResult.valid(null)
    if (raw.isBlank()) {
        badRequest("Query parameter '$paramName' must not be blank.", "INVALID_USER_ID")
        return TourParseResult.invalid()
    }
    return TourParseResult.valid(UserId(raw))
}

private suspend fun ApplicationCall.parseStrictBoolean(raw: String?): TourParseResult<Boolean> {
    if (raw == null) return TourParseResult.valid(null)
    if (raw.isBlank()) {
        badRequest("Query parameter 'activeOnly' must not be blank.", "INVALID_ACTIVE_ONLY")
        return TourParseResult.invalid()
    }

    return when (raw) {
        "true" -> TourParseResult.valid(true)
        "false" -> TourParseResult.valid(false)
        else -> {
            badRequest("Query parameter 'activeOnly' must be 'true' or 'false'.", "INVALID_ACTIVE_ONLY")
            TourParseResult.invalid()
        }
    }
}

private class TourParseResult<T> private constructor(
    val value: T?,
    val isValid: Boolean,
) {
    companion object {
        fun <T> valid(value: T?): TourParseResult<T> = TourParseResult(value = value, isValid = true)

        fun <T> invalid(): TourParseResult<T> = TourParseResult(value = null, isValid = false)
    }
}

private suspend fun ApplicationCall.badRequest(message: String, code: String) {
    respond(HttpStatusCode.BadRequest, ErrorResponseDto(message = message, code = code))
}

private suspend fun ApplicationCall.notFound(message: String, code: String) {
    respond(HttpStatusCode.NotFound, ErrorResponseDto(message = message, code = code))
}
