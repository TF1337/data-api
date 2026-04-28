package nl.freek.vgwm.data.entity

import nl.freek.vgwm.data.table.LocationsTable
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.Entity
import org.jetbrains.exposed.v1.dao.EntityClass

/**
 * Exposed DAO entity for rows stored in [LocationsTable].
 */
class LocationEntity(id: EntityID<String>) : Entity<String>(id) {
    companion object : EntityClass<String, LocationEntity>(LocationsTable)

    var name by LocationsTable.name
    var parentLocationId by LocationsTable.parentLocationId
    var orderIndex by LocationsTable.orderIndex
    var attentionNote by LocationsTable.attentionNote
}
