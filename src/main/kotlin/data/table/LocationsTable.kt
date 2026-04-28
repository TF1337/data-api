package nl.freek.vgwm.data.table

import org.jetbrains.exposed.v1.core.dao.id.IdTable

/**
 * Persistence schema for hierarchical company locations.
 */
object LocationsTable : IdTable<String>("locations") {
    override val id = varchar("id", 36).entityId()
    val name = varchar("name", 255)
    val parentLocationId = optReference(
        name = "parent_location_id",
        foreign = LocationsTable,
    )
    val orderIndex = integer("order_index")
    val attentionNote = text("attention_note").nullable()

    override val primaryKey = PrimaryKey(id)
}
