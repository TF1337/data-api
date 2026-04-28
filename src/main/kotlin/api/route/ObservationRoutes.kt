package nl.freek.vgwm.api.route

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import java.util.Base64
import kotlinx.datetime.Instant
import nl.freek.vgwm.api.dto.CreateObservationRequestDto
import nl.freek.vgwm.api.dto.ErrorResponseDto
import nl.freek.vgwm.api.dto.ObservationAiContextDto
import nl.freek.vgwm.api.dto.mapper.toDto
import nl.freek.vgwm.domain.id.CategoryId
import nl.freek.vgwm.domain.id.CheckpointId
import nl.freek.vgwm.domain.id.LocationId
import nl.freek.vgwm.domain.id.ObservationId
import nl.freek.vgwm.domain.id.TourId
import nl.freek.vgwm.domain.id.UserId
import nl.freek.vgwm.domain.repository.ObservationFilter
import nl.freek.vgwm.domain.repository.ObservationRepository
import nl.freek.vgwm.domain.service.NewObservationRequest
import nl.freek.vgwm.domain.service.ObservationAiContext
import nl.freek.vgwm.domain.service.ObservationCreationService
import nl.freek.vgwm.domain.value.ObservationStatus
import nl.freek.vgwm.domain.value.ObservationType
import nl.freek.vgwm.domain.value.Priority

/**
 * Registers observation HTTP routes.
 */
fun Route.observationRoutes(
    observationRepository: ObservationRepository,
    observationCreationService: ObservationCreationService,
) {
    authenticate("auth-jwt") {
        route("/api/observations") {
            get {
                val filter = call.buildObservationFilter(
                    tourIdRaw = call.request.queryParameters["tourId"],
                    checkpointIdRaw = call.request.queryParameters["checkpointId"],
                    locationIdRaw = call.request.queryParameters["locationId"],
                    statusRaw = call.request.queryParameters["status"],
                    priorityRaw = call.request.queryParameters["priority"],
                    observationTypeRaw = call.request.queryParameters["observationType"],
                    assigneeIdRaw = call.request.queryParameters["assigneeId"],
                    overdueOnlyRaw = call.request.queryParameters["overdueOnly"],
                )

                if (filter == null) {
                    return@get
                }

                val observations = observationRepository.findAll(filter).map { it.toDto() }
                call.respond(observations)
            }

            get("/{id}") {
                val id = call.parameters["id"]
                if (id.isNullOrBlank()) {
                    call.badRequest("Path parameter 'id' is required.", "INVALID_OBSERVATION_ID")
                    return@get
                }

                val observation = observationRepository.findById(ObservationId(id))
                if (observation == null) {
                    call.notFound("Observation not found.", "OBSERVATION_NOT_FOUND")
                    return@get
                }

                call.respond(observation.toDto())
            }

            post {
                val requestDto = call.receive<CreateObservationRequestDto>()
                val newObservationRequest = try {
                    requestDto.toNewObservationRequest()
                } catch (exception: IllegalArgumentException) {
                    call.badRequest(
                        message = exception.message ?: "Invalid observation create request.",
                        code = "INVALID_OBSERVATION_REQUEST",
                    )
                    return@post
                }

                val created = observationCreationService.createWithAiAnalysis(newObservationRequest)
                call.respond(HttpStatusCode.Created, created.toDto())
            }
        }
    }
}

private suspend fun ApplicationCall.buildObservationFilter(
    tourIdRaw: String?,
    checkpointIdRaw: String?,
    locationIdRaw: String?,
    statusRaw: String?,
    priorityRaw: String?,
    observationTypeRaw: String?,
    assigneeIdRaw: String?,
    overdueOnlyRaw: String?,
): ObservationFilter? {
    val tourId = parseTourId(tourIdRaw)
    if (!tourId.isValid) return null

    val checkpointId = parseCheckpointId(checkpointIdRaw)
    if (!checkpointId.isValid) return null

    val locationId = parseLocationId(locationIdRaw)
    if (!locationId.isValid) return null

    val status = parseObservationStatus(statusRaw)
    if (!status.isValid) return null

    val priority = parsePriority(priorityRaw)
    if (!priority.isValid) return null

    val observationType = parseObservationType(observationTypeRaw)
    if (!observationType.isValid) return null

    val assigneeId = parseUserId(assigneeIdRaw, "assigneeId")
    if (!assigneeId.isValid) return null

    val overdueOnly = parseStrictBoolean(overdueOnlyRaw)
    if (!overdueOnly.isValid) return null

    return ObservationFilter(
        tourId = tourId.value,
        checkpointId = checkpointId.value,
        locationId = locationId.value,
        status = status.value,
        priority = priority.value,
        observationType = observationType.value,
        assigneeId = assigneeId.value,
        overdueOnly = overdueOnly.value ?: false,
    )
}

private suspend fun ApplicationCall.parseTourId(raw: String?): ParseResult<TourId> {
    if (raw == null) return ParseResult.valid(null)
    if (raw.isBlank()) {
        badRequest("Query parameter 'tourId' must not be blank.", "INVALID_TOUR_ID")
        return ParseResult.invalid()
    }
    return ParseResult.valid(TourId(raw))
}

private suspend fun ApplicationCall.parseCheckpointId(raw: String?): ParseResult<CheckpointId> {
    if (raw == null) return ParseResult.valid(null)
    if (raw.isBlank()) {
        badRequest("Query parameter 'checkpointId' must not be blank.", "INVALID_CHECKPOINT_ID")
        return ParseResult.invalid()
    }
    return ParseResult.valid(CheckpointId(raw))
}

private suspend fun ApplicationCall.parseLocationId(raw: String?): ParseResult<LocationId> {
    if (raw == null) return ParseResult.valid(null)
    if (raw.isBlank()) {
        badRequest("Query parameter 'locationId' must not be blank.", "INVALID_LOCATION_ID")
        return ParseResult.invalid()
    }
    return ParseResult.valid(LocationId(raw))
}

private suspend fun ApplicationCall.parseUserId(raw: String?, paramName: String): ParseResult<UserId> {
    if (raw == null) return ParseResult.valid(null)
    if (raw.isBlank()) {
        badRequest("Query parameter '$paramName' must not be blank.", "INVALID_USER_ID")
        return ParseResult.invalid()
    }
    return ParseResult.valid(UserId(raw))
}

private suspend fun ApplicationCall.parseObservationStatus(raw: String?): ParseResult<ObservationStatus> {
    if (raw == null) return ParseResult.valid(null)
    if (raw.isBlank()) {
        badRequest("Query parameter 'status' must not be blank.", "INVALID_STATUS")
        return ParseResult.invalid()
    }

    return try {
        ParseResult.valid(ObservationStatus.valueOf(raw))
    } catch (_: IllegalArgumentException) {
        badRequest("Unknown observation status: $raw", "INVALID_STATUS")
        ParseResult.invalid()
    }
}

private suspend fun ApplicationCall.parseObservationType(raw: String?): ParseResult<ObservationType> {
    if (raw == null) return ParseResult.valid(null)
    if (raw.isBlank()) {
        badRequest("Query parameter 'observationType' must not be blank.", "INVALID_OBSERVATION_TYPE")
        return ParseResult.invalid()
    }

    return try {
        ParseResult.valid(ObservationType.valueOf(raw))
    } catch (_: IllegalArgumentException) {
        badRequest("Unknown observation type: $raw", "INVALID_OBSERVATION_TYPE")
        ParseResult.invalid()
    }
}

private suspend fun ApplicationCall.parsePriority(raw: String?): ParseResult<Priority> {
    if (raw == null) return ParseResult.valid(null)
    if (raw.isBlank()) {
        badRequest("Query parameter 'priority' must not be blank.", "INVALID_PRIORITY")
        return ParseResult.invalid()
    }

    return try {
        ParseResult.valid(Priority.fromCode(raw))
    } catch (_: IllegalArgumentException) {
        badRequest("Unknown priority code: $raw", "INVALID_PRIORITY")
        ParseResult.invalid()
    }
}

private suspend fun ApplicationCall.parseStrictBoolean(raw: String?): ParseResult<Boolean> {
    if (raw == null) return ParseResult.valid(null)

    return when (raw) {
        "true" -> ParseResult.valid(true)
        "false" -> ParseResult.valid(false)
        else -> {
            badRequest(
                "Query parameter 'overdueOnly' must be 'true' or 'false'.",
                "INVALID_OVERDUE_ONLY",
            )
            ParseResult.invalid()
        }
    }
}

private data class ParseResult<T>(
    val value: T?,
    val isValid: Boolean,
) {
    companion object {
        fun <T> valid(value: T?): ParseResult<T> = ParseResult(value = value, isValid = true)

        fun <T> invalid(): ParseResult<T> = ParseResult(value = null, isValid = false)
    }
}

private fun CreateObservationRequestDto.toNewObservationRequest(): NewObservationRequest = NewObservationRequest(
    checkpointId = CheckpointId(checkpointId),
    observerId = UserId(observerId),
    assigneeId = assigneeId?.let { UserId(it) },
    categoryId = CategoryId(categoryId),
    observationType = parseObservationTypeOrThrow(observationType, "observationType"),
    description = description,
    priority = parsePriorityOrThrow(priority),
    tourId = tourId?.let { TourId(it) },
    photoBytes = decodePhotoBase64(photoBase64),
    aiContext = aiContext.toDomain(),
)

private fun ObservationAiContextDto.toDomain(): ObservationAiContext = ObservationAiContext(
    checkpointLabel = checkpointLabel,
    locationLabel = locationLabel,
    observerLabel = observerLabel,
    assigneeLabel = assigneeLabel,
    observationType = parseObservationTypeOrThrow(observationType, "aiContext.observationType"),
    description = description,
    observedAt = parseObservedAt(observedAt),
)

private fun parseObservationTypeOrThrow(raw: String, fieldName: String): ObservationType = try {
    ObservationType.valueOf(raw)
} catch (_: IllegalArgumentException) {
    throw IllegalArgumentException("Invalid '$fieldName' value: $raw")
}

private fun parsePriorityOrThrow(raw: String): Priority = try {
    Priority.fromCode(raw)
} catch (_: IllegalArgumentException) {
    throw IllegalArgumentException("Invalid 'priority' value: $raw")
}

private fun decodePhotoBase64(raw: String?): ByteArray? {
    if (raw == null) {
        return null
    }

    return try {
        Base64.getDecoder().decode(raw)
    } catch (_: IllegalArgumentException) {
        throw IllegalArgumentException("Invalid 'photoBase64' value.")
    }
}

private fun parseObservedAt(raw: String): Instant = try {
    Instant.parse(raw)
} catch (_: IllegalArgumentException) {
    throw IllegalArgumentException("Invalid 'aiContext.observedAt' value: $raw")
}

private suspend fun ApplicationCall.badRequest(message: String, code: String) {
    respond(HttpStatusCode.BadRequest, ErrorResponseDto(message = message, code = code))
}

private suspend fun ApplicationCall.notFound(message: String, code: String) {
    respond(HttpStatusCode.NotFound, ErrorResponseDto(message = message, code = code))
}