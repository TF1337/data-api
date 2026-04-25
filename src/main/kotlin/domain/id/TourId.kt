package nl.freek.vgwm.domain.id

import java.util.UUID

/**
 * Strongly typed identifier for a tour.
 */
@JvmInline
value class TourId(val value: String) {
    init {
        require(value.isNotBlank()) { "TourId value must not be blank." }
    }

    companion object {
        /**
         * Generates a new tour identifier backed by a random UUID.
         */
        fun generate(): TourId = TourId(UUID.randomUUID().toString())
    }
}
