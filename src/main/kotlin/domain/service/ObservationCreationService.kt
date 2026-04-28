package nl.freek.vgwm.domain.service

import nl.freek.vgwm.domain.id.CategoryId
import nl.freek.vgwm.domain.id.CheckpointId
import nl.freek.vgwm.domain.id.TourId
import nl.freek.vgwm.domain.id.UserId
import nl.freek.vgwm.domain.value.ObservationType
import nl.freek.vgwm.domain.value.Priority

/**
 * Request model for observation creation orchestration.
 *
 * TODO(requirement): Implement ObservationCreationService orchestration in Section 16 item 14B.
 */
data class NewObservationRequest(
    val checkpointId: CheckpointId,
    val observerId: UserId,
    val assigneeId: UserId? = null,
    val categoryId: CategoryId,
    val observationType: ObservationType,
    val description: String,
    val priority: Priority,
    val tourId: TourId? = null,
    val photoBytes: ByteArray? = null,
    val aiContext: ObservationAiContext,
) {
    init {
        require(description.isNotBlank()) { "New observation description must not be blank." }
    }
}
