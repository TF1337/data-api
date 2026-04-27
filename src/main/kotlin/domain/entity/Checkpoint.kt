package nl.freek.vgwm.domain.entity

import nl.freek.vgwm.domain.base.Entity
import nl.freek.vgwm.domain.id.CategoryId
import nl.freek.vgwm.domain.id.CheckpointId
import nl.freek.vgwm.domain.id.LocationId

/**
 * Inspection checkpoint reference linked to a location and applicable categories.
 */
class Checkpoint private constructor(
    override val id: CheckpointId,
    val locationId: LocationId,
    val name: String,
    val attentionNote: String?,
    val applicableCategories: Set<CategoryId>,
) : Entity() {
    /**
     * Returns whether this checkpoint is relevant for the supplied category.
     */
    fun isCategoryApplicable(categoryId: CategoryId): Boolean = categoryId in applicableCategories

    companion object {
        /**
         * Creates a new checkpoint with generated identity and validated invariants.
         */
        fun create(
            locationId: LocationId,
            name: String,
            attentionNote: String? = null,
            applicableCategories: Set<CategoryId>,
        ): Checkpoint = Checkpoint(
            id = CheckpointId.generate(),
            locationId = locationId,
            name = name.trim(),
            attentionNote = attentionNote,
            applicableCategories = applicableCategories,
        ).validated()

        /**
         * Reconstructs a persisted checkpoint while keeping domain invariants enforced.
         */
        internal fun rehydrate(
            id: CheckpointId,
            locationId: LocationId,
            name: String,
            attentionNote: String?,
            applicableCategories: Set<CategoryId>,
        ): Checkpoint = Checkpoint(
            id = id,
            locationId = locationId,
            name = name.trim(),
            attentionNote = attentionNote,
            applicableCategories = applicableCategories,
        ).validated()
    }

    private fun validated(): Checkpoint {
        require(name.isNotBlank()) { "Checkpoint name must not be blank." }
        attentionNote?.let {
            require(it.isNotBlank()) { "Checkpoint attentionNote must not be blank when provided." }
        }
        require(applicableCategories.isNotEmpty()) { "Checkpoint applicableCategories must not be empty." }
        return this
    }
}
