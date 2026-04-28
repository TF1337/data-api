package nl.freek.vgwm.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt
import nl.freek.vgwm.config.AppConfig

fun Application.configureAuth(appConfig: AppConfig) {
    val algorithm = Algorithm.HMAC256(appConfig.jwt.secret)
    val verifier = JWT.require(algorithm)
        .withIssuer(appConfig.jwt.issuer)
        .withAudience(appConfig.jwt.audience)
        .build()

    install(Authentication) {
        jwt("auth-jwt") {
            this.verifier(verifier)
            validate { credential ->
                val subject = credential.payload.subject
                if (subject.isNullOrBlank()) {
                    null
                } else {
                    JWTPrincipal(credential.payload)
                }
            }
        }
    }
}
