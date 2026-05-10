package nl.freek.vgwm.api.route

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.patch
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import nl.freek.vgwm.api.dto.CreateTourRequestDto
import nl.freek.vgwm.api.dto.ErrorResponseDto
import nl.freek.vgwm.api.dto.UpdateTourCheckpointRequestDto
import nl.freek.vgwm.api.dto.mapper.toDto
import nl.freek.vgwm.domain.id.CheckpointId
import nl.freek.vgwm.domain.id.LocationId
import nl.freek.vgwm.domain.id.TourId
import nl.freek.vgwm.domain.id.UserId
import nl.freek.vgwm.domain.service.TourCheckpointProgressService
import nl.freek.vgwm.domain.service.TourCreationService
import nl.freek.vgwm.domain.service.TourLifecycleService
import nl.freek.vgwm.domain.repository.TourFilter
import nl.freek.vgwm.domain.repository.TourRepository
import nl.freek.vgwm.domain.value.CheckpointStatus
import nl.freek.vgwm.domain.value.TourStatus
import nl.freek.vgwm.domain.value.TourType
import org.koin.ktor.ext.get

/**
 * Registers read-only tour HTTP routes.
 */
fun Route.tourRoutes(tourRepository: TourRepository) {
    authenticate("auth-jwt") {
        route("/api/tours") {
            post {
                val tourCreationService = call.application.get<TourCreationService>()
                val request = call.receive<CreateTourRequestDto>()

                val locationId = call.parseLocationId(request.locationId)
                if (!locationId.isValid) {
                    return@post
                }

                val startedBy = call.parseStartedById(request.startedBy)
                if (!startedBy.isValid) {
                    return@post
                }

                val type = call.parseCreateTourType(request.type)
                if (!type.isValid) {
                    return@post
                }

                val participants = call.parseParticipants(request.participants)
                if (!participants.isValid) {
                    return@post
                }

                val createRequest = TourCreationService.CreateAndStartTourRequest(
                    locationId = locationId.value ?: return@post,
                    type = type.value ?: return@post,
                    startedBy = startedBy.value ?: return@post,
                    participants = participants.value ?: return@post,
                )

                val createdTour = try {
                    tourCreationService.createAndStart(
                        createRequest
                    )
                } catch (_: TourCreationService.NoCheckpointsForLocationException) {
                    call.badRequest(
                        message = "No checkpoints found for location.",
                        code = "NO_CHECKPOINTS_FOR_LOCATION",
                    )
                    return@post
                }

                call.respond(HttpStatusCode.Created, createdTour.toDto())
            }

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

            patch("/{id}/checkpoints/{checkpointId}") {
                val progressService = call.application.get<TourCheckpointProgressService>()

                val tourId = call.parseTourPathId(call.parameters["id"])
                if (!tourId.isValid) {
                    return@patch
                }

                val checkpointId = call.parseCheckpointPathId(call.parameters["checkpointId"])
                if (!checkpointId.isValid) {
                    return@patch
                }

                val request = call.receive<UpdateTourCheckpointRequestDto>()
                val status = call.parseCheckpointStatus(request.status)
                if (!status.isValid) {
                    return@patch
                }

                val updateRequest = TourCheckpointProgressService.UpdateTourCheckpointRequest(
                    tourId = tourId.value ?: return@patch,
                    checkpointId = checkpointId.value ?: return@patch,
                    status = status.value ?: return@patch,
                    notes = request.notes,
                )

                val updatedTour = try {
                    progressService.updateCheckpoint(updateRequest)
                } catch (_: TourCheckpointProgressService.TourNotFoundException) {
                    call.notFound("Tour not found.", "TOUR_NOT_FOUND")
                    return@patch
                } catch (_: TourCheckpointProgressService.CheckpointNotInTourException) {
                    call.notFound("Checkpoint not found in tour.", "CHECKPOINT_NOT_FOUND_IN_TOUR")
                    return@patch
                } catch (_: TourCheckpointProgressService.TourNotInProgressException) {
                    call.badRequest("Tour is not in progress.", "TOUR_NOT_IN_PROGRESS")
                    return@patch
                }

                call.respond(HttpStatusCode.OK, updatedTour.toDto())
            }

            post("/{id}/end") {
                val lifecycleService = call.application.get<TourLifecycleService>()
                val tourId = call.parseTourPathId(call.parameters["id"])
                if (!tourId.isValid) {
                    return@post
                }

                val endedTour = try {
                    lifecycleService.endTour(tourId.value ?: return@post)
                } catch (_: TourLifecycleService.TourNotFoundException) {
                    call.notFound("Tour not found.", "TOUR_NOT_FOUND")
                    return@post
                } catch (exception: TourLifecycleService.InvalidLifecycleTransitionException) {
                    call.badRequest(
                        exception.message ?: "Invalid tour lifecycle transition.",
                        "INVALID_TOUR_LIFECYCLE_TRANSITION",
                    )
                    return@post
                }

                call.respond(HttpStatusCode.OK, endedTour.toDto())
            }

            post("/{id}/cancel") {
                val lifecycleService = call.application.get<TourLifecycleService>()
                val tourId = call.parseTourPathId(call.parameters["id"])
                if (!tourId.isValid) {
                    return@post
                }

                val cancelledTour = try {
                    lifecycleService.cancelTour(tourId.value ?: return@post)
                } catch (_: TourLifecycleService.TourNotFoundException) {
                    call.notFound("Tour not found.", "TOUR_NOT_FOUND")
                    return@post
                } catch (exception: TourLifecycleService.InvalidLifecycleTransitionException) {
                    call.badRequest(
                        exception.message ?: "Invalid tour lifecycle transition.",
                        "INVALID_TOUR_LIFECYCLE_TRANSITION",
                    )
                    return@post
                }

                call.respond(HttpStatusCode.OK, cancelledTour.toDto())
            }
        }
    }
}

private suspend fun ApplicationCall.parseTourPathId(raw: String?): TourParseResult<TourId> {
    if (raw == null || raw.isBlank()) {
        badRequest("Path parameter 'id' is required and must not be blank.", "INVALID_TOUR_ID")
        return TourParseResult.invalid()
    }

    return try {
        TourParseResult.valid(TourId(raw))
    } catch (_: IllegalArgumentException) {
        badRequest("Invalid 'id' value.", "INVALID_TOUR_ID")
        TourParseResult.invalid()
    }
}

private suspend fun ApplicationCall.parseCheckpointPathId(raw: String?): TourParseResult<CheckpointId> {
    if (raw == null || raw.isBlank()) {
        badRequest("Path parameter 'checkpointId' is required and must not be blank.", "INVALID_CHECKPOINT_ID")
        return TourParseResult.invalid()
    }

    return try {
        TourParseResult.valid(CheckpointId(raw))
    } catch (_: IllegalArgumentException) {
        badRequest("Invalid 'checkpointId' value.", "INVALID_CHECKPOINT_ID")
        TourParseResult.invalid()
    }
}

private suspend fun ApplicationCall.parseCheckpointStatus(raw: String): TourParseResult<CheckpointStatus> {
    if (raw.isBlank()) {
        badRequest("Field 'status' is required and must not be blank.", "INVALID_CHECKPOINT_STATUS")
        return TourParseResult.invalid()
    }

    return try {
        TourParseResult.valid(CheckpointStatus.valueOf(raw))
    } catch (_: IllegalArgumentException) {
        badRequest("Unknown checkpoint status: $raw", "INVALID_CHECKPOINT_STATUS")
        TourParseResult.invalid()
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

private suspend fun ApplicationCall.parseCreateTourType(raw: String?): TourParseResult<TourType> {
    if (raw == null || raw.isBlank()) {
        badRequest("Field 'type' is required and must not be blank.", "INVALID_TOUR_TYPE")
        return TourParseResult.invalid()
    }

    return try {
        TourParseResult.valid(TourType.valueOf(raw))
    } catch (_: IllegalArgumentException) {
        badRequest("Unknown tour type: $raw", "INVALID_TOUR_TYPE")
        TourParseResult.invalid()
    }
}

private suspend fun ApplicationCall.parseLocationId(raw: String): TourParseResult<LocationId> {
    if (raw.isBlank()) {
        badRequest("Field 'locationId' is required and must not be blank.", "INVALID_LOCATION_ID")
        return TourParseResult.invalid()
    }

    return try {
        TourParseResult.valid(LocationId(raw))
    } catch (_: IllegalArgumentException) {
        badRequest("Invalid 'locationId' value.", "INVALID_LOCATION_ID")
        TourParseResult.invalid()
    }
}

private suspend fun ApplicationCall.parseStartedById(raw: String): TourParseResult<UserId> {
    if (raw.isBlank()) {
        badRequest("Field 'startedBy' is required and must not be blank.", "INVALID_STARTED_BY")
        return TourParseResult.invalid()
    }

    return try {
        TourParseResult.valid(UserId(raw))
    } catch (_: IllegalArgumentException) {
        badRequest("Invalid 'startedBy' value.", "INVALID_STARTED_BY")
        TourParseResult.invalid()
    }
}

private suspend fun ApplicationCall.parseParticipants(raw: List<String>): TourParseResult<Set<UserId>> {
    val participants = mutableSetOf<UserId>()
    raw.forEach { participantId ->
        if (participantId.isBlank()) {
            badRequest("Participant ID must not be blank.", "INVALID_PARTICIPANT_ID")
            return TourParseResult.invalid()
        }

        try {
            participants += UserId(participantId)
        } catch (_: IllegalArgumentException) {
            badRequest("Invalid participant ID: $participantId", "INVALID_PARTICIPANT_ID")
            return TourParseResult.invalid()
        }
    }

    return TourParseResult.valid(participants)
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
