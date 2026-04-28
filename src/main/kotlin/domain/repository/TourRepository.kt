package nl.freek.vgwm.domain.repository

import nl.freek.vgwm.domain.entity.Tour
import nl.freek.vgwm.domain.id.TourId

/**
 * Repository contract for managing tour aggregates.
 */
interface TourRepository {
    /**
     * Finds a single tour by its unique identity.
     */
    suspend fun findById(id: TourId): Tour?

    /**
     * Retrieves tours matching the supplied [filter].
     */
    suspend fun findAll(filter: TourFilter = TourFilter()): List<Tour>

    /**
     * Persists the tour and returns the saved domain state.
     */
    suspend fun save(tour: Tour): Tour

    /**
     * Deletes a tour by id.
     *
     * @return true when a tour was deleted, false when no matching record existed.
     */
    suspend fun delete(id: TourId): Boolean
}
