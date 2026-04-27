package nl.freek.vgwm.domain.value

/**
 * Lifecycle states for a tour execution.
 */
enum class TourStatus {
    NOT_STARTED,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED;

    /**
     * Indicates whether the tour can no longer transition to active execution states.
     */
    val isTerminal: Boolean
        get() = this == COMPLETED || this == CANCELLED

    /**
     * Indicates whether checkpoint progress updates are allowed in this state.
     */
    val allowsModification: Boolean
        get() = this == IN_PROGRESS
}
