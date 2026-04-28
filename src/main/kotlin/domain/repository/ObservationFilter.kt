package nl.freek.vgwm.domain.repository

import nl.freek.vgwm.domain.id.CheckpointId
import nl.freek.vgwm.domain.id.LocationId
import nl.freek.vgwm.domain.id.TourId
import nl.freek.vgwm.domain.id.UserId
import nl.freek.vgwm.domain.value.ObservationStatus
import nl.freek.vgwm.domain.value.ObservationType
import nl.freek.vgwm.domain.value.Priority

/**
 * Query parameters for retrieving observations with optional filtering criteria.
 */
data class ObservationFilter(
    val tourId: TourId? = null,
    val checkpointId: CheckpointId? = null,
    val locationId: LocationId? = null,
    val status: ObservationStatus? = null,
    val priority: Priority? = null,
    val observationType: ObservationType? = null,
    val assigneeId: UserId? = null,
    val overdueOnly: Boolean = false,
)