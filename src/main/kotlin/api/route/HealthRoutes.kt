package nl.freek.vgwm.api.route

import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.get

/**
 * Registers health-check HTTP routes.
 */
fun Route.healthRoutes() {
    get("/health") {
        call.respondText("OK")
    }
}
