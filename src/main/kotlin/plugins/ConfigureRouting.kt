package nl.freek.vgwm.plugins

import io.ktor.server.application.Application
import io.ktor.server.routing.routing
import nl.freek.vgwm.api.route.authRoutes
import nl.freek.vgwm.api.route.categoryRoutes
import nl.freek.vgwm.api.route.checkpointRoutes
import nl.freek.vgwm.api.route.companyInfoRoutes
import nl.freek.vgwm.api.route.healthRoutes
import nl.freek.vgwm.api.route.locationRoutes
import nl.freek.vgwm.api.route.observationRoutes
import nl.freek.vgwm.api.route.photoRoutes
import nl.freek.vgwm.api.route.tourRoutes
import nl.freek.vgwm.api.route.userRoutes
import org.koin.ktor.ext.get

fun Application.configureRouting() {
    routing {
        healthRoutes()
        authRoutes(get())
        companyInfoRoutes(get())
        locationRoutes(get())
        categoryRoutes(get())
        checkpointRoutes(get())
        observationRoutes(get(), get())
        tourRoutes(get())
        userRoutes(get())
        photoRoutes(get())
    }
}
