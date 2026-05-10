package nl.freek.vgwm.api.route

import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.response.respond
import io.ktor.server.response.respondBytes
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import nl.freek.vgwm.api.dto.ErrorResponseDto
import nl.freek.vgwm.domain.service.PhotoStorage

private val SAFE_PHOTO_KEY_REGEX = Regex("^[A-Za-z0-9._-]+$")

/**
 * Registers photo retrieval HTTP routes.
 */
fun Route.photoRoutes(photoStorage: PhotoStorage) {
    authenticate("auth-jwt") {
        route("/api/photos") {
            get("/{uuid}") {
                val photoKey = call.parameters["uuid"]
                if (photoKey.isNullOrBlank()) {
                    call.respond(
                        status = HttpStatusCode.BadRequest,
                        message = ErrorResponseDto(
                            message = "Path parameter 'uuid' is required.",
                            code = "INVALID_PHOTO_ID",
                        )
                    )
                    return@get
                }

                if (!isSafePhotoKey(photoKey)) {
                    call.respond(
                        status = HttpStatusCode.BadRequest,
                        message = ErrorResponseDto(
                            message = "Path parameter 'uuid' contains unsafe characters.",
                            code = "INVALID_PHOTO_ID",
                        )
                    )
                    return@get
                }

                val photoBytes = photoStorage.load(photoKey)
                if (photoBytes == null) {
                    call.respond(
                        status = HttpStatusCode.NotFound,
                        message = ErrorResponseDto(
                            message = "Photo was not found.",
                            code = "PHOTO_NOT_FOUND",
                        )
                    )
                    return@get
                }

                call.respondBytes(
                    bytes = photoBytes,
                    contentType = ContentType.Application.OctetStream,
                )
            }
        }
    }
}

private fun isSafePhotoKey(photoKey: String): Boolean {
    if (!SAFE_PHOTO_KEY_REGEX.matches(photoKey)) {
        return false
    }
    if (photoKey.contains("..")) {
        return false
    }
    return true
}
