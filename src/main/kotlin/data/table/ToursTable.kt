package nl.freek.vgwm.data.table

import nl.freek.vgwm.domain.value.TourStatus
import nl.freek.vgwm.domain.value.TourType
import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.IdTable
import org.jetbrains.exposed.v1.datetime.timestamp

/**
 * Persistence schema for tours.
 */
object ToursTable : IdTable<String>("tours") {
    override val id = varchar("id", 36).entityId()
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")
    val type = enumerationByName("type", 32, TourType::class)
    val startedBy = reference(
        name = "started_by",
        foreign = UsersTable,
        onDelete = ReferenceOption.RESTRICT,
    )
    val status = enumerationByName("status", 32, TourStatus::class)
    val startedAt = timestamp("started_at").nullable()
    val endedAt = timestamp("ended_at").nullable()

    override val primaryKey = PrimaryKey(id)
}
