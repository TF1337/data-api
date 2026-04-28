package nl.freek.vgwm.data.entity

import nl.freek.vgwm.data.table.CheckpointsTable
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.Entity
import org.jetbrains.exposed.v1.dao.EntityClass

/**
 * Exposed DAO entity for rows stored in [CheckpointsTable].
 */
class CheckpointEntity(id: EntityID<String>) : Entity<String>(id) {
    companion object : EntityClass<String, CheckpointEntity>(CheckpointsTable)

    var locationId by CheckpointsTable.locationId
    var name by CheckpointsTable.name
    var attentionNote by CheckpointsTable.attentionNote
}
