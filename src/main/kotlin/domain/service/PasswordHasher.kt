package nl.freek.vgwm.domain.service

import nl.freek.vgwm.domain.value.PasswordHash

/**
 * Strategy contract for password hashing and verification.
 */
interface PasswordHasher {
    /**
     * Hashes a raw password into a validated domain hash wrapper.
     */
    fun hash(raw: String): PasswordHash

    /**
     * Verifies a raw password against a stored hash.
     */
    fun verify(raw: String, hash: PasswordHash): Boolean
}
