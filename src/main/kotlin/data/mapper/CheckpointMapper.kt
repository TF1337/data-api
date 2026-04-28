package nl.freek.vgwm.data.mapper

import nl.freek.vgwm.data.entity.CheckpointEntity
import nl.freek.vgwm.data.table.LocationsTable
import nl.freek.vgwm.domain.entity.Checkpoint
import nl.freek.vgwm.domain.id.CategoryId
import nl.freek.vgwm.domain.id.CheckpointId
import nl.freek.vgwm.domain.id.LocationId
import org.jetbrains.exposed.v1.core.dao.id.EntityID

/**
 * Maps between [CheckpointEntity] and domain [Checkpoint].
 */
object CheckpointMapper {
    fun toDomain(
        entity: CheckpointEntity,
        applicableCategoryIds: Set<CategoryId>,
    ): Checkpoint = Checkpoint.rehydrate(
        id = CheckpointId(entity.id.value),
        locationId = LocationId(entity.locationId.value),
        name = entity.name,
        attentionNote = entity.attentionNote,
        applicableCategories = applicableCategoryIds,
    )

    fun applyToEntity(domain: Checkpoint, entity: CheckpointEntity) {
        entity.locationId = EntityID(domain.locationId.value, LocationsTable)
        entity.name = domain.name
        entity.attentionNote = domain.attentionNote
    }
}