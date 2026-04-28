package nl.freek.vgwm.data.table

import nl.freek.vgwm.domain.value.CheckpointStatus
import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.datetime.timestamp

/**
 * Child table storing tour-owned checkpoint progress snapshots.
 */
object TourCheckpointProgressTable : Table("tour_checkpoint_progress") {
    val tourId = reference(
        name = "tour_id",
        foreign = ToursTable,
        onDelete = ReferenceOption.CASCADE,
    )
    val checkpointId = reference(
        name = "checkpoint_id",
        foreign = CheckpointsTable,
        onDelete = ReferenceOption.RESTRICT,
    )
    val status = enumerationByName("status", 32, CheckpointStatus::class)
    val visitedAt = timestamp("visited_at").nullable()
    val notes = text("notes").nullable()

    override val primaryKey = PrimaryKey(tourId, checkpointId)
}
