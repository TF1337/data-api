package nl.freek.vgwm.data.repository

import nl.freek.vgwm.data.entity.TourEntity
import nl.freek.vgwm.data.mapper.TourMapper
import nl.freek.vgwm.data.table.TourCheckpointProgressTable
import nl.freek.vgwm.data.table.TourParticipantsTable
import nl.freek.vgwm.data.table.ToursTable
import nl.freek.vgwm.data.table.UsersTable
import nl.freek.vgwm.domain.entity.Tour
import nl.freek.vgwm.domain.id.CheckpointId
import nl.freek.vgwm.domain.id.TourId
import nl.freek.vgwm.domain.id.UserId
import nl.freek.vgwm.domain.repository.TourFilter
import nl.freek.vgwm.domain.repository.TourRepository
import nl.freek.vgwm.domain.value.TourCheckpointProgress
import nl.freek.vgwm.domain.value.TourStatus
import org.jetbrains.exposed.v1.core.Op
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.inList
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.experimental.newSuspendedTransaction

/**
 * Exposed-backed implementation of [TourRepository].
 *
 * Uses DAO for the main tour aggregate row and localized DSL for owned relation tables.
 */
class ExposedTourRepository : TourRepository {

    override suspend fun findById(id: TourId): Tour? = newSuspendedTransaction {
        val entity = TourEntity.findById(id.value) ?: return@newSuspendedTransaction null
        val participants = loadParticipants(id.value)
        val checkpointProgress = loadCheckpointProgress(id.value)
        mapTour(entity, participants, checkpointProgress)
    }

    override suspend fun findAll(filter: TourFilter): List<Tour> = newSuspendedTransaction {
        val participantTourIds = filter.participantId?.let { participantId ->
            loadTourIdsForParticipant(participantId.value)
        }

        if (filter.participantId != null && participantTourIds.isNullOrEmpty()) {
            return@newSuspendedTransaction emptyList()
        }

        val query = ToursTable
            .selectAll()
            .where { buildFilterPredicate(filter, participantTourIds) }

        val entities = TourEntity.wrapRows(query).toList()
        if (entities.isEmpty()) {
            return@newSuspendedTransaction emptyList()
        }

        val tourIds = entities.map { it.id.value }
        val participantsByTourId = loadParticipants(tourIds)
        val progressByTourId = loadCheckpointProgress(tourIds)

        entities.map { entity ->
            mapTour(
                entity = entity,
                participants = participantsByTourId[entity.id.value].orEmpty(),
                checkpointProgress = progressByTourId[entity.id.value].orEmpty(),
            )
        }
    }

    override suspend fun save(tour: Tour): Tour = newSuspendedTransaction {
        val existing = TourEntity.findById(tour.id.value)
        val entity = existing ?: TourEntity.new(tour.id.value) {}

        TourMapper.applyToEntity(tour, entity)
        replaceParticipants(tour)
        replaceCheckpointProgress(tour)

        mapTour(
            entity = entity,
            participants = tour.participants,
            checkpointProgress = tour.checkpointProgress,
        )
    }

    override suspend fun delete(id: TourId): Boolean = newSuspendedTransaction {
        val entity = TourEntity.findById(id.value) ?: return@newSuspendedTransaction false

        TourParticipantsTable.deleteWhere { TourParticipantsTable.tourId eq entity.id }
        TourCheckpointProgressTable.deleteWhere { TourCheckpointProgressTable.tourId eq entity.id }
        entity.delete()
        true
    }

    /**
     * Loads participants for a single tour id.
     */
    private fun loadParticipants(tourId: String): Set<UserId> = TourParticipantsTable
        .selectAll()
        .where { TourParticipantsTable.tourId eq tourId }
        .map { row -> UserId(row[TourParticipantsTable.userId].value) }
        .toSet()

    /**
     * Batch-loads participants grouped by tour id to avoid per-tour relation queries.
     */
    private fun loadParticipants(tourIds: List<String>): Map<String, Set<UserId>> {
        if (tourIds.isEmpty()) return emptyMap()

        return TourParticipantsTable
            .selectAll()
            .where { TourParticipantsTable.tourId inList tourIds }
            .groupBy(
                keySelector = { row -> row[TourParticipantsTable.tourId].value },
                valueTransform = { row -> UserId(row[TourParticipantsTable.userId].value) },
            )
            .mapValues { (_, users) -> users.toSet() }
    }

    /**
     * Loads checkpoint progress entries for a single tour id.
     */
    private fun loadCheckpointProgress(tourId: String): List<TourCheckpointProgress> = TourCheckpointProgressTable
        .selectAll()
        .where { TourCheckpointProgressTable.tourId eq tourId }
        .map { row ->
            TourCheckpointProgress(
                checkpointId = CheckpointId(row[TourCheckpointProgressTable.checkpointId].value),
                status = row[TourCheckpointProgressTable.status],
                visitedAt = row[TourCheckpointProgressTable.visitedAt]?.toDomainInstant(),
                notes = row[TourCheckpointProgressTable.notes],
            )
        }

    /**
     * Batch-loads checkpoint progress grouped by tour id.
     */
    private fun loadCheckpointProgress(tourIds: List<String>): Map<String, List<TourCheckpointProgress>> {
        if (tourIds.isEmpty()) return emptyMap()

        return TourCheckpointProgressTable
            .selectAll()
            .where { TourCheckpointProgressTable.tourId inList tourIds }
            .groupBy(
                keySelector = { row -> row[TourCheckpointProgressTable.tourId].value },
                valueTransform = { row ->
                    TourCheckpointProgress(
                        checkpointId = CheckpointId(row[TourCheckpointProgressTable.checkpointId].value),
                        status = row[TourCheckpointProgressTable.status],
                        visitedAt = row[TourCheckpointProgressTable.visitedAt]?.toDomainInstant(),
                        notes = row[TourCheckpointProgressTable.notes],
                    )
                },
            )
    }

    /**
     * Replaces all participant rows for a tour with the current domain aggregate state.
     */
    private fun replaceParticipants(tour: Tour) {
        val tourEntityId = EntityID(tour.id.value, ToursTable)
        TourParticipantsTable.deleteWhere { TourParticipantsTable.tourId eq tourEntityId }
        tour.participants.forEach { participantId ->
            TourParticipantsTable.insert { row ->
                row[tourId] = tourEntityId
                row[userId] = EntityID(participantId.value, UsersTable)
            }
        }
    }

    /**
     * Replaces all checkpoint progress rows for a tour with the current domain aggregate state.
     */
    private fun replaceCheckpointProgress(tour: Tour) {
        val tourEntityId = EntityID(tour.id.value, ToursTable)
        TourCheckpointProgressTable.deleteWhere { TourCheckpointProgressTable.tourId eq tourEntityId }
        tour.checkpointProgress.forEach { progress ->
            TourCheckpointProgressTable.insert { row ->
                row[tourId] = tourEntityId
                row[checkpointId] = progress.checkpointId.value
                row[status] = progress.status
                row[visitedAt] = progress.visitedAt?.toPersistenceInstant()
                row[notes] = progress.notes
            }
        }
    }

    /**
     * Maps a persisted tour entity and relation values into a domain aggregate.
     */
    private fun mapTour(
        entity: TourEntity,
        participants: Set<UserId>,
        checkpointProgress: List<TourCheckpointProgress>,
    ): Tour = TourMapper.toDomain(entity, participants, checkpointProgress)

    /**
     * Loads tour ids where the given participant is linked in the junction table.
     */
    private fun loadTourIdsForParticipant(participantId: String): Set<String> = TourParticipantsTable
        .selectAll()
        .where { TourParticipantsTable.userId eq participantId }
        .map { row -> row[TourParticipantsTable.tourId].value }
        .toSet()

    /**
     * Builds a single predicate for all database-side tour filters.
     */
    private fun buildFilterPredicate(filter: TourFilter, participantTourIds: Set<String>?): Op<Boolean> {
        var predicate: Op<Boolean> = Op.TRUE

        filter.status?.let { status ->
            predicate = predicate and (ToursTable.status eq status)
        }
        filter.type?.let { type ->
            predicate = predicate and (ToursTable.type eq type)
        }

        // Exposed requires typed EntityID values for this foreign key comparison.
        filter.startedBy?.let { startedBy ->
            predicate = predicate and (ToursTable.startedBy eq EntityID(startedBy.value, UsersTable))
        }

        if (filter.activeOnly) {
            predicate = predicate and (ToursTable.status inList listOf(TourStatus.NOT_STARTED, TourStatus.IN_PROGRESS))
        }

        participantTourIds?.let { tourIds ->
            predicate = predicate and (ToursTable.id inList tourIds)
        }

        return predicate
    }
}

private fun kotlin.time.Instant.toDomainInstant(): kotlinx.datetime.Instant =
    kotlinx.datetime.Instant.parse(toString())

private fun kotlinx.datetime.Instant.toPersistenceInstant(): kotlin.time.Instant =
    kotlin.time.Instant.parse(toString())