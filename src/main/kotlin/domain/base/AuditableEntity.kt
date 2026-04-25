package nl.freek.vgwm.domain.base

import kotlinx.datetime.Instant

/**
 * Base type for domain entities that track lifecycle timestamps.
 */
abstract class AuditableEntity : Entity() {
    /**
     * Timestamp when the entity was created.
     */
    abstract val createdAt: Instant

    /**
     * Timestamp when the entity was last updated.
     */
    abstract val updatedAt: Instant
}
