package nl.freek.vgwm.data.repository

import nl.freek.vgwm.data.entity.CheckpointEntity
import nl.freek.vgwm.data.mapper.CheckpointMapper
import nl.freek.vgwm.data.table.CheckpointCategoriesTable
import nl.freek.vgwm.data.table.CheckpointsTable
import nl.freek.vgwm.domain.entity.Checkpoint
import nl.freek.vgwm.domain.id.CategoryId
import nl.freek.vgwm.domain.id.CheckpointId
import nl.freek.vgwm.domain.id.LocationId
import nl.freek.vgwm.domain.repository.CheckpointRepository
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.inList
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.experimental.newSuspendedTransaction

/**
 * Exposed-backed implementation of [CheckpointRepository].
 *
 * Uses DAO for checkpoint records and localized DSL for [CheckpointCategoriesTable], which has no DAO entity.
 */
class ExposedCheckpointRepository : CheckpointRepository {

    override suspend fun findById(id: CheckpointId): Checkpoint? = newSuspendedTransaction {
        val entity = CheckpointEntity.findById(id.value) ?: return@newSuspendedTransaction null
        val categories = loadApplicableCategories(setOf(id))[id].orEmpty()
        CheckpointMapper.toDomain(entity, categories)
    }

    override suspend fun findByLocation(locationId: LocationId): List<Checkpoint> = newSuspendedTransaction {
        val entities = CheckpointEntity.find { CheckpointsTable.locationId eq locationId.value }
            .orderBy(CheckpointsTable.name to SortOrder.ASC)
            .toList()

        mapCheckpointsWithCategories(entities)
    }

    override suspend fun findAll(): List<Checkpoint> = newSuspendedTransaction {
        val entities = CheckpointEntity.all()
            .orderBy(CheckpointsTable.name to SortOrder.ASC)
            .toList()

        mapCheckpointsWithCategories(entities)
    }

    /**
     * Maps checkpoint entities with their applicable categories in a batched manner.
     */
    private fun mapCheckpointsWithCategories(entities: List<CheckpointEntity>): List<Checkpoint> {
        if (entities.isEmpty()) {
            return emptyList()
        }

        val checkpointIds = entities.map { CheckpointId(it.id.value) }.toSet()
        val categoriesByCheckpoint = loadApplicableCategories(checkpointIds)

        return entities.map { entity ->
            val checkpointId = CheckpointId(entity.id.value)
            val categories = categoriesByCheckpoint[checkpointId].orEmpty()
            CheckpointMapper.toDomain(entity, categories)
        }
    }

    /**
     * Loads applicable category ids for checkpoints from [CheckpointCategoriesTable].
     */
    private fun loadApplicableCategories(checkpointIds: Set<CheckpointId>): Map<CheckpointId, Set<CategoryId>> {
        if (checkpointIds.isEmpty()) {
            return emptyMap()
        }

        return CheckpointCategoriesTable
            .selectAll()
            .where { CheckpointCategoriesTable.checkpointId inList checkpointIds.map { it.value } }
            .groupBy(
                keySelector = { row -> CheckpointId(row[CheckpointCategoriesTable.checkpointId].value) },
                valueTransform = { row -> CategoryId(row[CheckpointCategoriesTable.categoryId].value) },
            )
            .mapValues { (_, categoryIds) -> categoryIds.toSet() }
    }
}
