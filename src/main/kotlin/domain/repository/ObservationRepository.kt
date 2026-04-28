package nl.freek.vgwm.domain.repository

import nl.freek.vgwm.domain.entity.Observation
import nl.freek.vgwm.domain.id.ObservationId

/**
 * Repository contract for managing observation aggregates.
 */
interface ObservationRepository {
    /**
     * Finds a single observation by its unique identity.
     */
    suspend fun findById(id: ObservationId): Observation?

    /**
     * Retrieves observations matching the supplied [filter].
     */
    suspend fun findAll(filter: ObservationFilter = ObservationFilter()): List<Observation>

    /**
     * Persists the observation and returns the saved domain state.
     */
    suspend fun save(observation: Observation): Observation

    /**
     * Deletes an observation by id.
     *
     * @return true when an observation was deleted, false when no matching record existed.
     */
    suspend fun delete(id: ObservationId): Boolean
}