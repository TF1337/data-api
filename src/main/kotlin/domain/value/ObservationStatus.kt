package nl.freek.vgwm.domain.value

/**
 * Lifecycle states for an observation.
 */
enum class ObservationStatus {
    OPEN,
    IN_PROGRESS,
    RESOLVED,
    CANCELLED;

    /**
     * Indicates whether no further processing is expected for this status.
     */
    val isTerminal: Boolean
        get() = this == RESOLVED || this == CANCELLED
}
