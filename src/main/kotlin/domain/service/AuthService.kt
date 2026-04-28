package nl.freek.vgwm.domain.service

import kotlinx.datetime.Instant
import nl.freek.vgwm.domain.id.UserId
import nl.freek.vgwm.domain.repository.UserRepository

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

/**
 * Application service for authentication orchestration.
 *
 * This service validates login input contracts, verifies credentials through [PasswordHasher],
 * and delegates token operations to [TokenProvider].
 */
class AuthService(
    private val users: UserRepository,
    private val passwordHasher: PasswordHasher,
    private val tokenProvider: TokenProvider,
) {
    /**
     * Attempts login and returns an [AuthToken] when credentials are valid.
     *
     * @return token on success, or null when credentials are invalid.
     * @throws IllegalArgumentException when email or password is blank.
     */
    suspend fun login(email: String, password: String): AuthToken? {
        require(email.isNotBlank()) { "Login email must not be blank." }
        require(password.isNotBlank()) { "Login password must not be blank." }

        val normalizedEmail = email.trim().lowercase()
        val user = users.findByEmail(normalizedEmail) ?: return null
        val isValidPassword = passwordHasher.verify(password, user.passwordHash)
        if (!isValidPassword) {
            return null
        }

        return tokenProvider.issueToken(user.id)
    }

    /**
     * Verifies an authentication token and returns the related user identity when valid.
     */
    fun verifyToken(token: String): UserId? {
        if (token.isBlank()) {
            return null
        }
        return tokenProvider.verifyToken(token)
    }
}
