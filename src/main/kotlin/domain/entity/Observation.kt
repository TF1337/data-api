package nl.freek.vgwm.domain.entity

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import nl.freek.vgwm.domain.base.AuditableEntity
import nl.freek.vgwm.domain.id.CategoryId
import nl.freek.vgwm.domain.id.CheckpointId
import nl.freek.vgwm.domain.id.ObservationId
import nl.freek.vgwm.domain.id.TourId
import nl.freek.vgwm.domain.id.UserId
import nl.freek.vgwm.domain.value.AiAnalysis
import nl.freek.vgwm.domain.value.ObservationStatus
import nl.freek.vgwm.domain.value.ObservationType
import nl.freek.vgwm.domain.value.Priority

/**
 * Immutable observation recorded during inspections, with lifecycle behavior and deadline semantics.
 */
class Observation private constructor(
    override val id: ObservationId,
    override val createdAt: Instant,
    override val updatedAt: Instant,
    val tourId: TourId?,
    val checkpointId: CheckpointId,
    val observerId: UserId,
    val assigneeId: UserId?,
    val categoryId: CategoryId,
    val observationType: ObservationType,
    val description: String,
    val priority: Priority,
    val status: ObservationStatus,
    val photoPath: String?,
    val externalRef: String?,
    val aiAnalysis: AiAnalysis?,
) : AuditableEntity() {

    /**
     * Deadline derived from [priority] and [createdAt].
     */
    val deadline: Instant
        get() = priority.deadlineFrom(createdAt)

    /**
     * Returns whether this observation is overdue at the provided [now] moment.
     */
    fun isOverdue(now: Instant = Clock.System.now()): Boolean = !status.isTerminal && now > deadline

    /**
     * Marks this observation as resolved.
     */
    fun resolve(now: Instant = Clock.System.now()): Observation {
        require(now >= createdAt) { "Observation updatedAt must not be earlier than createdAt." }
        require(status == ObservationStatus.OPEN || status == ObservationStatus.IN_PROGRESS) {
            "Observation can only be resolved from OPEN or IN_PROGRESS status."
        }
        return copy(status = ObservationStatus.RESOLVED, updatedAt = now)
    }

    /**
     * Cancels this observation while it is still active.
     */
    fun cancel(now: Instant = Clock.System.now()): Observation {
        require(now >= createdAt) { "Observation updatedAt must not be earlier than createdAt." }
        require(!status.isTerminal) { "Observation cannot be cancelled from terminal status $status." }
        return copy(status = ObservationStatus.CANCELLED, updatedAt = now)
    }

    /**
     * Assigns the observation to a user while it is still active.
     */
    fun assignTo(userId: UserId, now: Instant = Clock.System.now()): Observation {
        require(now >= createdAt) { "Observation updatedAt must not be earlier than createdAt." }
        require(!status.isTerminal) { "Observation cannot be assigned in terminal status $status." }
        return copy(assigneeId = userId, updatedAt = now)
    }

    /**
     * Sets an external reference and updates the modification timestamp.
     */
    fun withExternalRef(externalRef: String, now: Instant = Clock.System.now()): Observation {
        require(now >= createdAt) { "Observation updatedAt must not be earlier than createdAt." }
        val normalizedExternalRef = normalizeOptionalText(externalRef, "Observation externalRef")
        return copy(externalRef = normalizedExternalRef, updatedAt = now)
    }

    /**
     * Stores AI-generated analysis details and updates the modification timestamp.
     */
    fun withAiAnalysis(aiAnalysis: AiAnalysis, now: Instant = Clock.System.now()): Observation {
        require(now >= createdAt) { "Observation updatedAt must not be earlier than createdAt." }
        return copy(aiAnalysis = aiAnalysis, updatedAt = now)
    }

    /**
     * Replaces the free-text description and updates the modification timestamp.
     */
    fun updateDescription(description: String, now: Instant = Clock.System.now()): Observation {
        require(now >= createdAt) { "Observation updatedAt must not be earlier than createdAt." }
        return copy(description = normalizeRequiredText(description, "Observation description"), updatedAt = now)
    }

    /**
     * Sets a photo path and updates the modification timestamp.
     */
    fun withPhotoPath(photoPath: String, now: Instant = Clock.System.now()): Observation {
        require(now >= createdAt) { "Observation updatedAt must not be earlier than createdAt." }
        val normalizedPhotoPath = normalizeOptionalText(photoPath, "Observation photoPath")
        return copy(photoPath = normalizedPhotoPath, updatedAt = now)
    }

    private fun copy(
        id: ObservationId = this.id,
        createdAt: Instant = this.createdAt,
        updatedAt: Instant = this.updatedAt,
        tourId: TourId? = this.tourId,
        checkpointId: CheckpointId = this.checkpointId,
        observerId: UserId = this.observerId,
        assigneeId: UserId? = this.assigneeId,
        categoryId: CategoryId = this.categoryId,
        observationType: ObservationType = this.observationType,
        description: String = this.description,
        priority: Priority = this.priority,
        status: ObservationStatus = this.status,
        photoPath: String? = this.photoPath,
        externalRef: String? = this.externalRef,
        aiAnalysis: AiAnalysis? = this.aiAnalysis,
    ): Observation = Observation(
        id = id,
        createdAt = createdAt,
        updatedAt = updatedAt,
        tourId = tourId,
        checkpointId = checkpointId,
        observerId = observerId,
        assigneeId = assigneeId,
        categoryId = categoryId,
        observationType = observationType,
        description = description,
        priority = priority,
        status = status,
        photoPath = photoPath,
        externalRef = externalRef,
        aiAnalysis = aiAnalysis,
    )

    private fun validated(): Observation {
        require(description.isNotBlank()) { "Observation description must not be blank." }
        require(updatedAt >= createdAt) { "Observation updatedAt must not be earlier than createdAt." }
        photoPath?.let {
            require(it.isNotBlank()) { "Observation photoPath must not be blank when provided." }
        }
        externalRef?.let {
            require(it.isNotBlank()) { "Observation externalRef must not be blank when provided." }
        }
        return this
    }

    companion object {
        /**
         * Creates a new observation with generated identity and validated invariant state.
         */
        fun create(
            checkpointId: CheckpointId,
            observerId: UserId,
            categoryId: CategoryId,
            observationType: ObservationType,
            description: String,
            priority: Priority,
            tourId: TourId? = null,
            photoPath: String? = null,
            now: Instant = Clock.System.now(),
        ): Observation = Observation(
            id = ObservationId.generate(),
            createdAt = now,
            updatedAt = now,
            tourId = tourId,
            checkpointId = checkpointId,
            observerId = observerId,
            assigneeId = null,
            categoryId = categoryId,
            observationType = observationType,
            description = normalizeRequiredText(description, "Observation description"),
            priority = priority,
            status = ObservationStatus.OPEN,
            photoPath = photoPath?.let { normalizeOptionalText(it, "Observation photoPath") },
            externalRef = null,
            aiAnalysis = null,
        ).validated()

        /**
         * Reconstructs a persisted observation while enforcing domain invariants.
         */
        internal fun rehydrate(
            id: ObservationId,
            createdAt: Instant,
            updatedAt: Instant,
            tourId: TourId?,
            checkpointId: CheckpointId,
            observerId: UserId,
            assigneeId: UserId?,
            categoryId: CategoryId,
            observationType: ObservationType,
            description: String,
            priority: Priority,
            status: ObservationStatus,
            photoPath: String?,
            externalRef: String?,
            aiAnalysis: AiAnalysis?,
        ): Observation = Observation(
            id = id,
            createdAt = createdAt,
            updatedAt = updatedAt,
            tourId = tourId,
            checkpointId = checkpointId,
            observerId = observerId,
            assigneeId = assigneeId,
            categoryId = categoryId,
            observationType = observationType,
            description = normalizeRequiredText(description, "Observation description"),
            priority = priority,
            status = status,
            photoPath = photoPath?.let { normalizeOptionalText(it, "Observation photoPath") },
            externalRef = externalRef?.let { normalizeOptionalText(it, "Observation externalRef") },
            aiAnalysis = aiAnalysis,
        ).validated()

        private fun normalizeRequiredText(value: String, fieldName: String): String {
            val normalized = value.trim()
            require(normalized.isNotBlank()) { "$fieldName must not be blank." }
            return normalized
        }

        private fun normalizeOptionalText(value: String, fieldName: String): String {
            val normalized = value.trim()
            require(normalized.isNotBlank()) { "$fieldName must not be blank when provided." }
            return normalized
        }
    }
}