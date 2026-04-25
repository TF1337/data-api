package nl.freek.vgwm.domain.id

import java.util.UUID

/**
 * Strongly typed identifier for a location.
 */
@JvmInline
value class LocationId(val value: String) {
    init {
        require(value.isNotBlank()) { "LocationId value must not be blank." }
    }

    companion object {
        /**
         * Generates a new location identifier backed by a random UUID.
         */
        fun generate(): LocationId = LocationId(UUID.randomUUID().toString())
    }
}
