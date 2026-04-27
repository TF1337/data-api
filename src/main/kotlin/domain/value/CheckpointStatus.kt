package nl.freek.vgwm.domain.value

/**
 * Visit/result status for a checkpoint during a tour.
 */
enum class CheckpointStatus {
    NOT_VISITED,
    OK,
    NEEDS_ATTENTION,
    ACTION_REQUIRED,
}
