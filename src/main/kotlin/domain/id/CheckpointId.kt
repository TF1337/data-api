package nl.freek.vgwm.domain.id

import java.util.UUID

/**
 * Strongly typed identifier for a checkpoint.
 */
@JvmInline
value class CheckpointId(val value: String) {
    init {
        require(value.isNotBlank()) { "CheckpointId value must not be blank." }
    }

    companion object {
        /**
         * Generates a new checkpoint identifier backed by a random UUID.
         */
        fun generate(): CheckpointId = CheckpointId(UUID.randomUUID().toString())
    }
}
