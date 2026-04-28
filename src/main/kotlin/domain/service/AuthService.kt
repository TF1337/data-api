package nl.freek.vgwm.domain.service

import kotlinx.datetime.Instant
import nl.freek.vgwm.domain.id.UserId

/**
 * Authentication token payload returned by authentication orchestration.
 */
data class AuthToken(
    val token: String,
    val expiresAt: Instant,
) {
    init {
        require(token.isNotBlank()) { "Auth token must not be blank." }
    }
}

/**
 * Strategy contract for token issuing and verification.
 *
 * TODO(requirement): Implement AuthService orchestration in Section 16 item 14C.
 */
interface TokenProvider {
    /**
     * Issues a token for the given user identity.
     */
    fun issueToken(userId: UserId): AuthToken

    /**
     * Verifies a token and returns the user identity when valid.
     */
    fun verifyToken(token: String): UserId?
}
