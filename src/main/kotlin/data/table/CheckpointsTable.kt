package nl.freek.vgwm.data.table

import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.IdTable

/**
 * Persistence schema for location checkpoints.
 */
object CheckpointsTable : IdTable<String>("checkpoints") {
    override val id = varchar("id", 36).entityId()
    val locationId = reference(
        name = "location_id",
        foreign = LocationsTable,
        onDelete = ReferenceOption.RESTRICT,
    )
    val name = varchar("name", 255)
    val attentionNote = text("attention_note").nullable()

    override val primaryKey = PrimaryKey(id)
}
