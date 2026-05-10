package nl.freek.vgwm.domain.service

import nl.freek.vgwm.domain.entity.Tour
import nl.freek.vgwm.domain.id.TourId
import nl.freek.vgwm.domain.repository.TourRepository

/**
 * Coordinates lifecycle transitions for tours.
 */
class TourLifecycleService(
    private val tours: TourRepository,
) {
    /**
     * Completes a tour using domain lifecycle behavior.
     */
    suspend fun endTour(tourId: TourId): Tour {
        val tour = tours.findById(tourId) ?: throw TourNotFoundException(tourId)
        val endedTour = try {
            tour.endTour()
        } catch (exception: IllegalArgumentException) {
            throw InvalidLifecycleTransitionException(exception.message ?: "Invalid tour lifecycle transition.")
        }
        return tours.save(endedTour)
    }

    /**
     * Cancels a tour using domain lifecycle behavior.
     */
    suspend fun cancelTour(tourId: TourId): Tour {
        val tour = tours.findById(tourId) ?: throw TourNotFoundException(tourId)
        val cancelledTour = try {
            tour.cancel()
        } catch (exception: IllegalArgumentException) {
            throw InvalidLifecycleTransitionException(exception.message ?: "Invalid tour lifecycle transition.")
        }
        return tours.save(cancelledTour)
    }

    /**
     * Raised when a requested tour does not exist.
     */
    class TourNotFoundException(tourId: TourId) :
        IllegalArgumentException("Tour ${tourId.value} was not found.")

    /**
     * Raised when a requested lifecycle transition is invalid.
     */
    class InvalidLifecycleTransitionException(message: String) : IllegalArgumentException(message)
}
