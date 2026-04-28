package nl.freek.vgwm.api.route

import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import nl.freek.vgwm.api.dto.ErrorResponseDto
import nl.freek.vgwm.api.dto.LoginRequestDto
import nl.freek.vgwm.api.dto.mapper.toDto
import nl.freek.vgwm.domain.service.AuthService

/**
 * Registers authentication HTTP routes.
 */
fun Route.authRoutes(authService: AuthService) {
    route("/api/auth") {
        post("/login") {
            val request = call.receive<LoginRequestDto>()
            val token = authService.login(email = request.email, password = request.password)

            if (token == null) {
                call.respond(
                    status = HttpStatusCode.Unauthorized,
                    message = ErrorResponseDto(
                        message = "Invalid credentials",
                        code = "INVALID_CREDENTIALS",
                    ),
                )
                return@post
            }

            call.respond(token.toDto())
        }
    }
}
