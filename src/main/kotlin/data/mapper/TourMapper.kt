package nl.freek.vgwm.data.mapper

import nl.freek.vgwm.data.entity.TourEntity
import nl.freek.vgwm.data.table.UsersTable
import nl.freek.vgwm.domain.entity.Tour
import nl.freek.vgwm.domain.id.TourId
import nl.freek.vgwm.domain.id.UserId
import nl.freek.vgwm.domain.value.TourCheckpointProgress
import org.jetbrains.exposed.v1.core.dao.id.EntityID

/**
 * Maps between [TourEntity] and domain [Tour].
 */
object TourMapper {
    fun toDomain(
        entity: TourEntity,
        participants: Set<UserId>,
        checkpointProgress: List<TourCheckpointProgress>,
    ): Tour = Tour.rehydrate(
        id = TourId(entity.id.value),
        createdAt = entity.createdAt.toDomainInstant(),
        updatedAt = entity.updatedAt.toDomainInstant(),
        type = entity.type,
        startedBy = UserId(entity.startedBy.value),
        status = entity.status,
        startedAt = entity.startedAt?.toDomainInstant(),
        endedAt = entity.endedAt?.toDomainInstant(),
        participants = participants,
        checkpointProgress = checkpointProgress,
    )

    fun applyToEntity(domain: Tour, entity: TourEntity) {
        entity.createdAt = domain.createdAt.toPersistenceInstant()
        entity.updatedAt = domain.updatedAt.toPersistenceInstant()
        entity.type = domain.type
        entity.startedBy = EntityID(domain.startedBy.value, UsersTable)
        entity.status = domain.status
        entity.startedAt = domain.startedAt?.toPersistenceInstant()
        entity.endedAt = domain.endedAt?.toPersistenceInstant()
    }
}

private fun kotlin.time.Instant.toDomainInstant(): kotlinx.datetime.Instant =
    kotlinx.datetime.Instant.parse(toString())

private fun kotlinx.datetime.Instant.toPersistenceInstant(): kotlin.time.Instant =
    kotlin.time.Instant.parse(toString())