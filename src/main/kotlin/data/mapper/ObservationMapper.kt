package nl.freek.vgwm.data.mapper

import nl.freek.vgwm.data.entity.ObservationEntity
import nl.freek.vgwm.data.table.CategoriesTable
import nl.freek.vgwm.data.table.CheckpointsTable
import nl.freek.vgwm.data.table.ToursTable
import nl.freek.vgwm.data.table.UsersTable
import nl.freek.vgwm.domain.entity.Observation
import nl.freek.vgwm.domain.id.CategoryId
import nl.freek.vgwm.domain.id.CheckpointId
import nl.freek.vgwm.domain.id.ObservationId
import nl.freek.vgwm.domain.id.TourId
import nl.freek.vgwm.domain.id.UserId
import nl.freek.vgwm.domain.value.Priority
import org.jetbrains.exposed.v1.core.dao.id.EntityID

/**
 * Maps between [ObservationEntity] and domain [Observation].
 */
object ObservationMapper {
    fun toDomain(entity: ObservationEntity): Observation = Observation.rehydrate(
        id = ObservationId(entity.id.value),
        createdAt = entity.createdAt.toDomainInstant(),
        updatedAt = entity.updatedAt.toDomainInstant(),
        tourId = entity.tourId?.value?.let(::TourId),
        checkpointId = CheckpointId(entity.checkpointId.value),
        observerId = UserId(entity.observerId.value),
        assigneeId = entity.assigneeId?.value?.let(::UserId),
        categoryId = CategoryId(entity.categoryId.value),
        observationType = entity.observationType,
        description = entity.description,
        priority = Priority.fromCode(entity.priorityCode),
        status = entity.status,
        photoPath = entity.photoPath,
        externalRef = entity.externalRef,
        aiAnalysis = AiAnalysisJsonMapper.fromJson(entity.aiAnalysisJson),
    )

    fun applyToEntity(domain: Observation, entity: ObservationEntity) {
        entity.createdAt = domain.createdAt.toPersistenceInstant()
        entity.updatedAt = domain.updatedAt.toPersistenceInstant()
        entity.tourId = domain.tourId?.let { EntityID(it.value, ToursTable) }
        entity.checkpointId = EntityID(domain.checkpointId.value, CheckpointsTable)
        entity.observerId = EntityID(domain.observerId.value, UsersTable)
        entity.assigneeId = domain.assigneeId?.let { EntityID(it.value, UsersTable) }
        entity.categoryId = EntityID(domain.categoryId.value, CategoriesTable)
        entity.observationType = domain.observationType
        entity.description = domain.description
        entity.priorityCode = domain.priority.code
        entity.status = domain.status
        entity.photoPath = domain.photoPath
        entity.externalRef = domain.externalRef
        entity.aiAnalysisJson = AiAnalysisJsonMapper.toJson(domain.aiAnalysis)
        entity.aiGeneratedAt = domain.aiAnalysis?.generatedAt?.toPersistenceInstant()
    }
}

private fun kotlin.time.Instant.toDomainInstant(): kotlinx.datetime.Instant =
    kotlinx.datetime.Instant.parse(toString())

private fun kotlinx.datetime.Instant.toPersistenceInstant(): kotlin.time.Instant =
    kotlin.time.Instant.parse(toString())