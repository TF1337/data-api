package nl.freek.vgwm.data.table

import nl.freek.vgwm.domain.value.ObservationStatus
import nl.freek.vgwm.domain.value.ObservationType
import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.IdTable
import org.jetbrains.exposed.v1.datetime.timestamp

/**
 * Persistence schema for observations, including AI analysis JSON payload.
 */
object ObservationsTable : IdTable<String>("observations") {
    override val id = varchar("id", 36).entityId()
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")
    val tourId = optReference(
        name = "tour_id",
        foreign = ToursTable,
        onDelete = ReferenceOption.SET_NULL,
    )
    val checkpointId = reference(
        name = "checkpoint_id",
        foreign = CheckpointsTable,
        onDelete = ReferenceOption.RESTRICT,
    )
    val observerId = reference(
        name = "observer_id",
        foreign = UsersTable,
        onDelete = ReferenceOption.RESTRICT,
    )
    val assigneeId = optReference(
        name = "assignee_id",
        foreign = UsersTable,
        onDelete = ReferenceOption.SET_NULL,
    )
    val categoryId = reference(
        name = "category_id",
        foreign = CategoriesTable,
        onDelete = ReferenceOption.RESTRICT,
    )
    val observationType = enumerationByName("observation_type", 32, ObservationType::class)
    val description = text("description")
    val priorityCode = varchar("priority_code", 16)
    val status = enumerationByName("status", 32, ObservationStatus::class)
    val photoPath = varchar("photo_path", 512).nullable()
    val externalRef = varchar("external_ref", 128).nullable()
    val aiAnalysisJson = text("ai_analysis_json").nullable()
    val aiGeneratedAt = timestamp("ai_generated_at").nullable()

    override val primaryKey = PrimaryKey(id)
}
