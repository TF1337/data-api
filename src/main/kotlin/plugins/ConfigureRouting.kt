package nl.freek.vgwm.plugins

import io.ktor.server.application.Application
import io.ktor.server.routing.routing
import nl.freek.vgwm.api.route.authRoutes
import nl.freek.vgwm.api.route.categoryRoutes
import nl.freek.vgwm.api.route.checkpointRoutes
import nl.freek.vgwm.api.route.healthRoutes
import nl.freek.vgwm.api.route.locationRoutes
import org.koin.ktor.ext.get

fun Application.configureRouting() {
    routing {
        healthRoutes()
        authRoutes(get())
        locationRoutes(get())
        categoryRoutes(get())
        checkpointRoutes(get())
    }
}
