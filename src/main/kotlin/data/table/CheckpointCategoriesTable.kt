package nl.freek.vgwm.data.table

import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.Table

/**
 * Junction table linking checkpoints and applicable categories.
 */
object CheckpointCategoriesTable : Table("checkpoint_categories") {
    val checkpointId = reference(
        name = "checkpoint_id",
        foreign = CheckpointsTable,
        onDelete = ReferenceOption.CASCADE,
    )
    val categoryId = reference(
        name = "category_id",
        foreign = CategoriesTable,
        onDelete = ReferenceOption.RESTRICT,
    )

    override val primaryKey = PrimaryKey(checkpointId, categoryId)
}
