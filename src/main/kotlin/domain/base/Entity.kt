package nl.freek.vgwm.domain.base

/**
 * Base type for all domain entities identified by an immutable [id].
 *
 * Equality is based on entity identity: same runtime class and same [id].
 */
abstract class Entity {
    /**
     * Unique identity of the entity.
     */
    abstract val id: Any

    final override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Entity) return false
        if (this::class != other::class) return false
        return id == other.id
    }

    final override fun hashCode(): Int = id.hashCode()
}
