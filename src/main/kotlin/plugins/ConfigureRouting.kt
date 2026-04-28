package nl.freek.vgwm.plugins

import io.ktor.server.application.Application
import io.ktor.server.routing.routing
import nl.freek.vgwm.api.route.healthRoutes

fun Application.configureRouting() {
    routing {
        healthRoutes()
    }
}
