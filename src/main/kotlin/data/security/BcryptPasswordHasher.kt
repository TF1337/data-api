package nl.freek.vgwm.data.security

import at.favre.lib.crypto.bcrypt.BCrypt
import nl.freek.vgwm.domain.service.PasswordHasher
import nl.freek.vgwm.domain.value.PasswordHash

/**
 * BCrypt-based implementation of [PasswordHasher].
 */
class BcryptPasswordHasher(
    private val cost: Int = 12,
) : PasswordHasher {
    init {
        require(cost in 4..31) { "BCrypt cost must be between 4 and 31." }
    }

    /**
     * Hashes a raw password with BCrypt and returns a validated [PasswordHash].
     */
    override fun hash(raw: String): PasswordHash {
        require(raw.isNotBlank()) { "Raw password must not be blank." }

        val encoded = BCrypt.withDefaults().hashToString(cost, raw.toCharArray())
        return PasswordHash(encoded)
    }

    /**
     * Verifies a raw password against a stored BCrypt [PasswordHash].
     */
    override fun verify(raw: String, hash: PasswordHash): Boolean {
        require(raw.isNotBlank()) { "Raw password must not be blank." }

        return BCrypt.verifyer().verify(raw.toCharArray(), hash.value).verified
    }
}
