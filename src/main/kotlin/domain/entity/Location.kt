package nl.freek.vgwm.domain.entity

import nl.freek.vgwm.domain.base.Entity
import nl.freek.vgwm.domain.id.LocationId

/**
 * Company location reference used to organize checkpoints in a hierarchy.
 */
class Location private constructor(
    override val id: LocationId,
    val name: String,
    val parentLocationId: LocationId?,
    val orderIndex: Int,
    val attentionNote: String?,
) : Entity() {
    /**
     * Indicates whether this location has no parent and is a top-level node.
     */
    val isTopLevel: Boolean
        get() = parentLocationId == null

    companion object {
        /**
         * Creates a new location with generated identity and validated invariants.
         */
        fun create(
            name: String,
            parentLocationId: LocationId? = null,
            orderIndex: Int,
            attentionNote: String? = null,
        ): Location = Location(
            id = LocationId.generate(),
            name = name.trim(),
            parentLocationId = parentLocationId,
            orderIndex = orderIndex,
            attentionNote = attentionNote?.trim(),
        ).validated()

        /**
         * Reconstructs a persisted location while keeping domain invariants enforced.
         */
        internal fun rehydrate(
            id: LocationId,
            name: String,
            parentLocationId: LocationId?,
            orderIndex: Int,
            attentionNote: String?,
        ): Location = Location(
            id = id,
            name = name.trim(),
            parentLocationId = parentLocationId,
            orderIndex = orderIndex,
            attentionNote = attentionNote?.trim(),
        ).validated()
    }

    private fun validated(): Location {
        require(name.isNotBlank()) { "Location name must not be blank." }
        require(orderIndex >= 0) { "Location orderIndex must be greater than or equal to zero." }
        attentionNote?.let {
            require(it.isNotBlank()) { "Location attentionNote must not be blank when provided." }
        }
        return this
    }
}
