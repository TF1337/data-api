package nl.freek.vgwm.domain.service

import nl.freek.vgwm.domain.entity.Tour
import nl.freek.vgwm.domain.id.LocationId
import nl.freek.vgwm.domain.id.UserId
import nl.freek.vgwm.domain.repository.CheckpointRepository
import nl.freek.vgwm.domain.repository.TourRepository
import nl.freek.vgwm.domain.value.CheckpointStatus
import nl.freek.vgwm.domain.value.TourCheckpointProgress
import nl.freek.vgwm.domain.value.TourType

/**
 * Coordinates tour creation from a selected location and immediately starts the tour.
 */
class TourCreationService(
    private val tours: TourRepository,
    private val checkpoints: CheckpointRepository,
) {
    /**
     * Creates and starts a tour from all checkpoints linked to [locationId].
     */
    suspend fun createAndStart(request: CreateAndStartTourRequest): Tour {
        val checkpointProgress = checkpoints.findByLocation(request.locationId)
            .map { checkpoint ->
                TourCheckpointProgress(
                    checkpointId = checkpoint.id,
                    status = CheckpointStatus.NOT_VISITED,
                    visitedAt = null,
                    notes = null,
                )
            }

        if (checkpointProgress.isEmpty()) {
            throw NoCheckpointsForLocationException(request.locationId)
        }

        val participants = request.participants + request.startedBy
        val tour = Tour.create(
            type = request.type,
            startedBy = request.startedBy,
            participants = participants,
            checkpointProgress = checkpointProgress,
        ).start()

        return tours.save(tour)
    }

    /**
     * Input model for create-and-start tour orchestration.
     */
    data class CreateAndStartTourRequest(
        val locationId: LocationId,
        val type: TourType,
        val startedBy: UserId,
        val participants: Set<UserId> = emptySet(),
    )

    /**
     * Raised when a location yields no checkpoints for tour initialization.
     */
    class NoCheckpointsForLocationException(locationId: LocationId) :
        IllegalArgumentException("No checkpoints found for location ${locationId.value}.")
}
