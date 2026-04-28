package nl.freek.vgwm.data.security

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTVerificationException
import java.time.Duration
import java.time.Instant as JavaInstant
import kotlinx.datetime.Instant
import nl.freek.vgwm.domain.id.UserId
import nl.freek.vgwm.domain.service.AuthToken
import nl.freek.vgwm.domain.service.TokenProvider

/**
 * JWT-based implementation of [TokenProvider].
 */
class JwtTokenProvider(
    secret: String,
    private val issuer: String,
    private val audience: String,
    private val expirationHours: Long,
) : TokenProvider {
    private val algorithm: Algorithm
    private val verifier by lazy { JWT.require(algorithm).withIssuer(issuer).withAudience(audience).build() }

    init {
        require(secret.isNotBlank()) { "JWT secret must not be blank." }
        require(issuer.isNotBlank()) { "JWT issuer must not be blank." }
        require(audience.isNotBlank()) { "JWT audience must not be blank." }
        require(expirationHours > 0) { "JWT expirationHours must be greater than 0." }
        algorithm = Algorithm.HMAC256(secret)
    }

    /**
     * Issues a signed JWT for the provided user identity.
     */
    override fun issueToken(userId: UserId): AuthToken {
        val expiresAt = JavaInstant.now().plus(Duration.ofHours(expirationHours))
        val token = JWT.create()
            .withSubject(userId.value)
            .withIssuer(issuer)
            .withAudience(audience)
            .withExpiresAt(expiresAt)
            .sign(algorithm)

        return AuthToken(
            token = token,
            expiresAt = Instant.fromEpochMilliseconds(expiresAt.toEpochMilli()),
        )
    }

    /**
     * Verifies the token signature and required claims, and returns the subject as [UserId].
     */
    override fun verifyToken(token: String): UserId? {
        if (token.isBlank()) {
            return null
        }

        return try {
            val decoded = verifier.verify(token)
            val subject = decoded.subject ?: return null
            UserId(subject)
        } catch (_: JWTVerificationException) {
            null
        } catch (_: IllegalArgumentException) {
            null
        }
    }
}
