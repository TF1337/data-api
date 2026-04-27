package nl.freek.vgwm.domain.value

import kotlinx.datetime.Instant
import nl.freek.vgwm.domain.id.CheckpointId

/**
 * Progress entry for a single checkpoint within a tour aggregate.
 */
data class TourCheckpointProgress(
    val checkpointId: CheckpointId,
    val status: CheckpointStatus,
    val visitedAt: Instant?,
    val notes: String?,
) {
    init {
        notes?.let {
            require(it.isNotBlank()) { "TourCheckpointProgress.notes must not be blank when provided." }
        }
    }

    /**
     * Indicates whether this checkpoint has transitioned away from the initial unvisited state.
     */
    val isVisited: Boolean
        get() = status != CheckpointStatus.NOT_VISITED
}
