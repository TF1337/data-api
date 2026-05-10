package nl.freek.vgwm.domain.service

import nl.freek.vgwm.domain.entity.Tour
import nl.freek.vgwm.domain.id.CheckpointId
import nl.freek.vgwm.domain.id.TourId
import nl.freek.vgwm.domain.repository.TourRepository
import nl.freek.vgwm.domain.value.CheckpointStatus
import nl.freek.vgwm.domain.value.TourStatus

/**
 * Coordinates checkpoint progress updates for active tours.
 */
class TourCheckpointProgressService(
    private val tours: TourRepository,
) {
    /**
     * Updates a single checkpoint progress entry for an in-progress tour.
     */
    suspend fun updateCheckpoint(request: UpdateTourCheckpointRequest): Tour {
        val tour = tours.findById(request.tourId) ?: throw TourNotFoundException(request.tourId)
        if (tour.status != TourStatus.IN_PROGRESS) {
            throw TourNotInProgressException(tour.id, tour.status)
        }
        if (tour.progressFor(request.checkpointId) == null) {
            throw CheckpointNotInTourException(request.tourId, request.checkpointId)
        }

        val updatedTour = tour.updateCheckpoint(
            checkpointId = request.checkpointId,
            status = request.status,
            notes = request.notes,
        )

        return tours.save(updatedTour)
    }

    /**
     * Input model for checkpoint progress updates.
     */
    data class UpdateTourCheckpointRequest(
        val tourId: TourId,
        val checkpointId: CheckpointId,
        val status: CheckpointStatus,
        val notes: String? = null,
    )

    /**
     * Raised when a requested tour does not exist.
     */
    class TourNotFoundException(tourId: TourId) :
        IllegalArgumentException("Tour ${tourId.value} was not found.")

    /**
     * Raised when checkpoint updates are attempted for a non-active tour.
     */
    class TourNotInProgressException(tourId: TourId, status: TourStatus) :
        IllegalArgumentException("Tour ${tourId.value} is not in progress (current status: $status).")

    /**
     * Raised when a checkpoint does not belong to the tour plan.
     */
    class CheckpointNotInTourException(tourId: TourId, checkpointId: CheckpointId) :
        IllegalArgumentException("Checkpoint ${checkpointId.value} is not part of tour ${tourId.value}.")
}
