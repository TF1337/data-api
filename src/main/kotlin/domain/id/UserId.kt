package nl.freek.vgwm.domain.id

import java.util.UUID

/**
 * Strongly typed identifier for a user.
 */
@JvmInline
value class UserId(val value: String) {
    init {
        require(value.isNotBlank()) { "UserId value must not be blank." }
    }

    companion object {
        /**
         * Generates a new user identifier backed by a random UUID.
         */
        fun generate(): UserId = UserId(UUID.randomUUID().toString())
    }
}
