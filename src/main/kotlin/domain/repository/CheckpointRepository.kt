package nl.freek.vgwm.domain.repository

import nl.freek.vgwm.domain.entity.Checkpoint
import nl.freek.vgwm.domain.id.CheckpointId
import nl.freek.vgwm.domain.id.LocationId

/**
 * Repository contract for querying checkpoint reference data.
 */
interface CheckpointRepository {
    /**
     * Finds a checkpoint by id.
     */
    suspend fun findById(id: CheckpointId): Checkpoint?

    /**
     * Returns checkpoints linked to a specific location.
     */
    suspend fun findByLocation(locationId: LocationId): List<Checkpoint>

    /**
     * Returns all configured checkpoints.
     */
    suspend fun findAll(): List<Checkpoint>
}
