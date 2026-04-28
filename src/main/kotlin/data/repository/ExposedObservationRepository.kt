package nl.freek.vgwm.data.repository

import nl.freek.vgwm.data.entity.ObservationEntity
import nl.freek.vgwm.data.mapper.ObservationMapper
import nl.freek.vgwm.data.table.CheckpointsTable
import nl.freek.vgwm.data.table.ObservationsTable
import nl.freek.vgwm.data.table.ToursTable
import nl.freek.vgwm.data.table.UsersTable
import nl.freek.vgwm.domain.entity.Observation
import nl.freek.vgwm.domain.id.ObservationId
import nl.freek.vgwm.domain.repository.ObservationFilter
import nl.freek.vgwm.domain.repository.ObservationRepository
import org.jetbrains.exposed.v1.core.Op
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.inList
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.experimental.newSuspendedTransaction

/**
 * Exposed-backed implementation of [ObservationRepository].
 *
 * Uses DAO for core CRUD operations and localized DSL for relation-aware filtering.
 */
class ExposedObservationRepository : ObservationRepository {

    override suspend fun findById(id: ObservationId): Observation? = newSuspendedTransaction {
        ObservationEntity.findById(id.value)?.let(ObservationMapper::toDomain)
    }

    override suspend fun findAll(filter: ObservationFilter): List<Observation> = newSuspendedTransaction {
        val locationCheckpointIds = filter.locationId?.let { locationId ->
            loadCheckpointIdsForLocation(locationId.value)
        }

        if (filter.locationId != null && locationCheckpointIds.isNullOrEmpty()) {
            return@newSuspendedTransaction emptyList()
        }

        val query = ObservationsTable
            .selectAll()
            .where { buildFilterPredicate(filter, locationCheckpointIds) }

        val observations = ObservationEntity.wrapRows(query)
            .map(ObservationMapper::toDomain)

        if (filter.overdueOnly) {
            observations.filter { it.isOverdue() }
        } else {
            observations
        }
    }

    override suspend fun save(observation: Observation): Observation = newSuspendedTransaction {
        val existing = ObservationEntity.findById(observation.id.value)
        val entity = existing ?: ObservationEntity.new(observation.id.value) {}

        ObservationMapper.applyToEntity(observation, entity)
        ObservationMapper.toDomain(entity)
    }

    override suspend fun delete(id: ObservationId): Boolean = newSuspendedTransaction {
        val entity = ObservationEntity.findById(id.value) ?: return@newSuspendedTransaction false
        entity.delete()
        true
    }

    /**
     * Loads checkpoint ids for a location to support relation-based observation filtering.
     */
    private fun loadCheckpointIdsForLocation(locationId: String): Set<String> = CheckpointsTable
        .selectAll()
        .where { CheckpointsTable.locationId eq locationId }
        .map { row -> row[CheckpointsTable.id].value }
        .toSet()

    /**
     * Builds a single SQL predicate for all database-side filters.
     */
    private fun buildFilterPredicate(
        filter: ObservationFilter,
        locationCheckpointIds: Set<String>?,
    ): Op<Boolean> {
        var predicate: Op<Boolean> = Op.TRUE

        // Exposed requires typed EntityID values for nullable FK comparisons.
        // ToursTable and UsersTable are referenced only to construct EntityID instances,
        // not for joins, relation loading, or additional table access.

        filter.tourId?.let { tourId ->
            predicate = predicate and (ObservationsTable.tourId eq EntityID(tourId.value, ToursTable))
        }
        filter.checkpointId?.let { checkpointId ->
            predicate = predicate and (ObservationsTable.checkpointId eq checkpointId.value)
        }
        filter.status?.let { status ->
            predicate = predicate and (ObservationsTable.status eq status)
        }
        filter.priority?.let { priority ->
            predicate = predicate and (ObservationsTable.priorityCode eq priority.code)
        }
        filter.observationType?.let { observationType ->
            predicate = predicate and (ObservationsTable.observationType eq observationType)
        }
        filter.assigneeId?.let { assigneeId ->
            predicate = predicate and (ObservationsTable.assigneeId eq EntityID(assigneeId.value, UsersTable))
        }
        locationCheckpointIds?.let { checkpointIds ->
            predicate = predicate and (ObservationsTable.checkpointId inList checkpointIds)
        }

        return predicate
    }
}
