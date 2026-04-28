package nl.freek.vgwm.data.entity

import nl.freek.vgwm.data.table.ToursTable
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.Entity
import org.jetbrains.exposed.v1.dao.EntityClass

/**
 * Exposed DAO entity for rows stored in [ToursTable].
 */
class TourEntity(id: EntityID<String>) : Entity<String>(id) {
    companion object : EntityClass<String, TourEntity>(ToursTable)

    var createdAt by ToursTable.createdAt
    var updatedAt by ToursTable.updatedAt
    var type by ToursTable.type
    var startedBy by ToursTable.startedBy
    var status by ToursTable.status
    var startedAt by ToursTable.startedAt
    var endedAt by ToursTable.endedAt
}
