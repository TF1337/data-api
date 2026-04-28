package nl.freek.vgwm

import io.ktor.server.application.Application
import io.ktor.server.application.install
import nl.freek.vgwm.config.AppConfig
import nl.freek.vgwm.di.appModule
import nl.freek.vgwm.plugins.configureAuth
import nl.freek.vgwm.plugins.configureCallLogging
import nl.freek.vgwm.plugins.configureHTTP
import nl.freek.vgwm.plugins.configureRouting
import nl.freek.vgwm.plugins.configureSerialization
import nl.freek.vgwm.plugins.configureStatusPages
import org.koin.ktor.plugin.Koin

fun Application.module() {
    val appConfig = AppConfig.fromEnvironment()

    install(Koin) {
        modules(appModule(appConfig))
    }

    configureCallLogging()
    configureStatusPages()
    configureHTTP()
    configureSerialization()
    configureAuth(appConfig)
    configureRouting()
}
