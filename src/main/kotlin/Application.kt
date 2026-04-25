package nl.freek.vgwm

import io.ktor.server.application.Application
import nl.freek.vgwm.plugins.configureAuth
import nl.freek.vgwm.plugins.configureCallLogging
import nl.freek.vgwm.plugins.configureHTTP
import nl.freek.vgwm.plugins.configureRouting
import nl.freek.vgwm.plugins.configureSerialization
import nl.freek.vgwm.plugins.configureStatusPages

fun Application.module() {
    configureCallLogging()
    configureStatusPages()
    configureHTTP()
    configureSerialization()
    configureAuth()
    configureRouting()
}
