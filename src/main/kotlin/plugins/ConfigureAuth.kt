package nl.freek.vgwm.plugins

import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication

fun Application.configureAuth() {
    install(Authentication) {
        // TODO(requirement): Configure JWT authentication in Section 16 item 17.
    }
}
