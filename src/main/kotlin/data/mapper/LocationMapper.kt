package nl.freek.vgwm.data.mapper

import nl.freek.vgwm.data.entity.LocationEntity
import nl.freek.vgwm.data.table.LocationsTable
import nl.freek.vgwm.domain.entity.Location
import nl.freek.vgwm.domain.id.LocationId
import org.jetbrains.exposed.v1.core.dao.id.EntityID

/**
 * Maps between [LocationEntity] and domain [Location].
 */
object LocationMapper {
    fun toDomain(entity: LocationEntity): Location = Location.rehydrate(
        id = LocationId(entity.id.value),
        name = entity.name,
        parentLocationId = entity.parentLocationId?.value?.let(::LocationId),
        orderIndex = entity.orderIndex,
        attentionNote = entity.attentionNote,
    )

    fun applyToEntity(domain: Location, entity: LocationEntity) {
        entity.name = domain.name
        entity.parentLocationId = domain.parentLocationId?.let { EntityID(it.value, LocationsTable) }
        entity.orderIndex = domain.orderIndex
        entity.attentionNote = domain.attentionNote
    }
}