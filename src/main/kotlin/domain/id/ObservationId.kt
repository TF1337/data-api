package nl.freek.vgwm.domain.id

import java.util.UUID

/**
 * Strongly typed identifier for an observation.
 */
@JvmInline
value class ObservationId(val value: String) {
    init {
        require(value.isNotBlank()) { "ObservationId value must not be blank." }
    }

    companion object {
        /**
         * Generates a new observation identifier backed by a random UUID.
         */
        fun generate(): ObservationId = ObservationId(UUID.randomUUID().toString())
    }
}
