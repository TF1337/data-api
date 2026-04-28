package nl.freek.vgwm.domain.entity

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import nl.freek.vgwm.domain.base.AuditableEntity
import nl.freek.vgwm.domain.id.UserId
import nl.freek.vgwm.domain.value.PasswordHash
import nl.freek.vgwm.domain.value.UserRole

/**
 * System user with role-based permissions and immutable profile state.
 */
class User private constructor(
    override val id: UserId,
    override val createdAt: Instant,
    override val updatedAt: Instant,
    val name: String,
    val email: String,
    val role: UserRole,
    val passwordHash: PasswordHash,
) : AuditableEntity() {

    /**
     * Returns whether this user can coordinate and manage tour execution.
     */
    fun canManageTours(): Boolean = role == UserRole.COORDINATOR || role == UserRole.MANAGER || role == UserRole.ADMIN

    /**
     * Returns whether this user can permanently remove observations.
     */
    fun canDeleteObservations(): Boolean = role == UserRole.MANAGER || role == UserRole.ADMIN

    /**
     * Changes the user role and updates the modification timestamp.
     */
    fun changeRole(newRole: UserRole, now: Instant = Clock.System.now()): User {
        require(now >= createdAt) { "User updatedAt must not be earlier than createdAt." }
        return copy(role = newRole, updatedAt = now)
    }

    /**
     * Replaces the password hash and updates the modification timestamp.
     */
    fun changePassword(newPasswordHash: PasswordHash, now: Instant = Clock.System.now()): User {
        require(now >= createdAt) { "User updatedAt must not be earlier than createdAt." }
        return copy(passwordHash = newPasswordHash, updatedAt = now)
    }

    /**
     * Updates profile fields using the same normalization and invariants as creation.
     */
    fun updateProfile(name: String, email: String, now: Instant = Clock.System.now()): User {
        require(now >= createdAt) { "User updatedAt must not be earlier than createdAt." }
        return copy(name = normalizeName(name), email = normalizeEmail(email), updatedAt = now).validated()
    }

    private fun copy(
        id: UserId = this.id,
        createdAt: Instant = this.createdAt,
        updatedAt: Instant = this.updatedAt,
        name: String = this.name,
        email: String = this.email,
        role: UserRole = this.role,
        passwordHash: PasswordHash = this.passwordHash,
    ): User = User(
        id = id,
        createdAt = createdAt,
        updatedAt = updatedAt,
        name = name,
        email = email,
        role = role,
        passwordHash = passwordHash,
    )

    private fun validated(): User {
        require(name.isNotBlank()) { "User name must not be blank." }
        require(email.isNotBlank()) { "User email must not be blank." }
        require(updatedAt >= createdAt) { "User updatedAt must not be earlier than createdAt." }
        require(isValidEmailShape(email)) { "User email must have a valid shape." }
        return this
    }

    companion object {
        /**
         * Creates a new user with generated identity and normalized, validated fields.
         */
        fun create(
            name: String,
            email: String,
            role: UserRole,
            passwordHash: PasswordHash,
            now: Instant = Clock.System.now(),
        ): User = User(
            id = UserId.generate(),
            createdAt = now,
            updatedAt = now,
            name = normalizeName(name),
            email = normalizeEmail(email),
            role = role,
            passwordHash = passwordHash,
        ).validated()

        /**
         * Reconstructs a persisted user while enforcing domain invariants.
         */
        internal fun rehydrate(
            id: UserId,
            createdAt: Instant,
            updatedAt: Instant,
            name: String,
            email: String,
            role: UserRole,
            passwordHash: PasswordHash,
        ): User = User(
            id = id,
            createdAt = createdAt,
            updatedAt = updatedAt,
            name = normalizeName(name),
            email = normalizeEmail(email),
            role = role,
            passwordHash = passwordHash,
        ).validated()

        private fun normalizeName(name: String): String = name.trim()

        private fun normalizeEmail(email: String): String = email.trim().lowercase()

        private fun isValidEmailShape(email: String): Boolean {
            val atIndex = email.indexOf('@')
            if (atIndex <= 0) return false
            if (atIndex != email.lastIndexOf('@')) return false
            if (atIndex >= email.lastIndex) return false

            val localPart = email.substring(0, atIndex)
            val domainPart = email.substring(atIndex + 1)

            if (localPart.isBlank() || domainPart.isBlank()) return false
            return domainPart.contains('.')
        }
    }
}