package nl.freek.vgwm.data.repository

import nl.freek.vgwm.data.entity.LocationEntity
import nl.freek.vgwm.data.mapper.LocationMapper
import nl.freek.vgwm.data.table.LocationsTable
import nl.freek.vgwm.domain.entity.Location
import nl.freek.vgwm.domain.id.LocationId
import nl.freek.vgwm.domain.repository.LocationRepository
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.jdbc.transactions.experimental.newSuspendedTransaction

/**
 * Exposed-backed implementation of [LocationRepository].
 */
class ExposedLocationRepository : LocationRepository {

    override suspend fun findAll(): List<Location> = newSuspendedTransaction {
        LocationEntity.all()
            .orderBy(LocationsTable.orderIndex to SortOrder.ASC, LocationsTable.name to SortOrder.ASC)
            .map(LocationMapper::toDomain)
    }

    override suspend fun findById(id: LocationId): Location? = newSuspendedTransaction {
        LocationEntity.findById(id.value)?.let(LocationMapper::toDomain)
    }

    override suspend fun findChildren(parentId: LocationId): List<Location> = newSuspendedTransaction {
        LocationEntity.find { LocationsTable.parentLocationId eq parentId.value }
            .orderBy(LocationsTable.orderIndex to SortOrder.ASC, LocationsTable.name to SortOrder.ASC)
            .map(LocationMapper::toDomain)
    }
}
