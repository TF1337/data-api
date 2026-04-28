package nl.freek.vgwm.domain.repository

import nl.freek.vgwm.domain.entity.Location
import nl.freek.vgwm.domain.id.LocationId

/**
 * Repository contract for querying location reference data.
 */
interface LocationRepository {
    /**
     * Returns all configured locations.
     */
    suspend fun findAll(): List<Location>

    /**
     * Finds a location by id.
     */
    suspend fun findById(id: LocationId): Location?

    /**
     * Returns direct child locations of the provided parent location.
     */
    suspend fun findChildren(parentId: LocationId): List<Location>
}
