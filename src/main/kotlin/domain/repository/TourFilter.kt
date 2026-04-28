package nl.freek.vgwm.domain.repository

import nl.freek.vgwm.domain.id.UserId
import nl.freek.vgwm.domain.value.TourStatus
import nl.freek.vgwm.domain.value.TourType

/**
 * Query parameters for retrieving tours with optional filtering criteria.
 */
data class TourFilter(
    val status: TourStatus? = null,
    val type: TourType? = null,
    val startedBy: UserId? = null,
    val participantId: UserId? = null,
    val activeOnly: Boolean = false,
)