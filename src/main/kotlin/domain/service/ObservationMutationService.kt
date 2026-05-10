package nl.freek.vgwm.domain.service

import nl.freek.vgwm.domain.entity.Observation
import nl.freek.vgwm.domain.id.ObservationId
import nl.freek.vgwm.domain.id.UserId
import nl.freek.vgwm.domain.repository.ObservationRepository
import nl.freek.vgwm.domain.repository.TourRepository
import nl.freek.vgwm.domain.value.AiAnalysis

/**
 * Coordinates mutation of existing observations using domain commands.
 */
class ObservationMutationService(
    private val observations: ObservationRepository,
    private val tours: TourRepository,
) {
    /**
     * Applies supported non-null updates to an observation and persists the result.
     *
     * @return updated observation, or null when no observation exists for [observationId].
     */
    suspend fun update(observationId: ObservationId, request: UpdateObservationRequest): Observation? {
        val existing = observations.findById(observationId) ?: return null
        ensureTourSupportsMutation(existing, request)
        val mutated = applyMutations(existing, request)
        return observations.save(mutated)
    }

    private suspend fun ensureTourSupportsMutation(observation: Observation, request: UpdateObservationRequest) {
        val tourId = observation.tourId ?: return
        val linkedTour = tours.findById(tourId) ?: throw IllegalArgumentException("TOUR_NOT_FOUND")

        if (linkedTour.status.isTerminal && request.hasOperationalContentChanges()) {
            throw IllegalArgumentException("TOUR_TERMINAL_OBSERVATION_MUTATION_NOT_ALLOWED")
        }
    }

    private fun applyMutations(observation: Observation, request: UpdateObservationRequest): Observation {
        var updated = observation

        request.description?.let {
            updated = updated.updateDescription(it)
        }

        request.assigneeId?.let {
            updated = updated.assignTo(UserId(it))
        }

        request.externalRef?.let {
            updated = updated.withExternalRef(it)
        }

        request.aiAnalysis?.let {
            updated = updated.withAiAnalysis(it)
        }

        request.status?.let {
            updated = applyStatusMutation(updated, it)
        }

        return updated
    }

    private fun applyStatusMutation(observation: Observation, requestedStatus: String): Observation =
        when (requestedStatus) {
            "RESOLVED" -> try {
                observation.resolve()
            } catch (_: IllegalArgumentException) {
                throw IllegalArgumentException("UNSUPPORTED_OBSERVATION_STATUS_TRANSITION")
            }

            "CANCELLED" -> try {
                observation.cancel()
            } catch (_: IllegalArgumentException) {
                throw IllegalArgumentException("UNSUPPORTED_OBSERVATION_STATUS_TRANSITION")
            }

            else -> throw IllegalArgumentException("INVALID_OBSERVATION_STATUS")
        }

    private fun UpdateObservationRequest.hasOperationalContentChanges(): Boolean =
        description != null || aiAnalysis != null
}

/**
 * Input for observation mutation orchestration.
 */
data class UpdateObservationRequest(
    val description: String? = null,
    val assigneeId: String? = null,
    val externalRef: String? = null,
    val aiAnalysis: AiAnalysis? = null,
    val status: String? = null,
)
