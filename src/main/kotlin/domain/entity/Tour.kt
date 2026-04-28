package nl.freek.vgwm.domain.entity

import kotlin.time.Duration
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import nl.freek.vgwm.domain.base.AuditableEntity
import nl.freek.vgwm.domain.id.CheckpointId
import nl.freek.vgwm.domain.id.TourId
import nl.freek.vgwm.domain.id.UserId
import nl.freek.vgwm.domain.value.CheckpointStatus
import nl.freek.vgwm.domain.value.TourCheckpointProgress
import nl.freek.vgwm.domain.value.TourStatus
import nl.freek.vgwm.domain.value.TourType

/**
 * Tour aggregate root that owns participant membership and per-checkpoint progress.
 */
class Tour private constructor(
    override val id: TourId,
    override val createdAt: Instant,
    override val updatedAt: Instant,
    val type: TourType,
    val startedBy: UserId,
    val status: TourStatus,
    val startedAt: Instant?,
    val endedAt: Instant?,
    val participants: Set<UserId>,
    val checkpointProgress: List<TourCheckpointProgress>,
) : AuditableEntity() {

    /**
     * Returns true only when all checkpoints are visited and at least one checkpoint exists.
     */
    val allCheckpointsVisited: Boolean
        get() = checkpointProgress.isNotEmpty() && checkpointProgress.all { it.isVisited }

    /**
     * Number of checkpoints currently marked as visited.
     */
    val visitedCount: Int
        get() = checkpointProgress.count { it.isVisited }

    /**
     * Total number of checkpoints in this tour plan.
     */
    val totalCheckpoints: Int
        get() = checkpointProgress.size

    /**
     * Runtime duration when both [startedAt] and [endedAt] are available.
     */
    val duration: Duration?
        get() = if (startedAt == null || endedAt == null) null else endedAt - startedAt

    /**
     * Returns checkpoint progress for the given checkpoint, or null when absent.
     */
    fun progressFor(checkpointId: CheckpointId): TourCheckpointProgress? =
        checkpointProgress.firstOrNull { it.checkpointId == checkpointId }

    /**
     * Returns whether the given user participates in this tour.
     */
    fun hasParticipant(userId: UserId): Boolean = userId in participants

    /**
     * Starts the tour execution.
     */
    fun start(now: Instant = Clock.System.now()): Tour {
        require(now >= createdAt) { "Tour updatedAt must not be earlier than createdAt." }
        require(status == TourStatus.NOT_STARTED) { "Tour can only be started from NOT_STARTED status." }
        require(participants.isNotEmpty()) { "Tour participants must not be empty." }

        return copy(
            status = TourStatus.IN_PROGRESS,
            startedAt = now,
            updatedAt = now,
        ).validated()
    }

    /**
     * Adds a participant before the tour has started.
     */
    fun addParticipant(userId: UserId, now: Instant = Clock.System.now()): Tour {
        require(now >= createdAt) { "Tour updatedAt must not be earlier than createdAt." }
        require(status == TourStatus.NOT_STARTED) { "Participants can only be added before tour start." }
        require(userId !in participants) { "User is already a participant of this tour." }

        return copy(
            participants = participants + userId,
            updatedAt = now,
        ).validated()
    }

    /**
     * Updates progress for one checkpoint while the tour is in a modifiable state.
     */
    fun updateCheckpoint(
        checkpointId: CheckpointId,
        status: CheckpointStatus,
        notes: String? = null,
        now: Instant = Clock.System.now(),
    ): Tour {
        require(now >= createdAt) { "Tour updatedAt must not be earlier than createdAt." }
        require(this.status.allowsModification) { "Checkpoint progress can only be updated while the tour is in progress." }
        require(checkpointProgress.any { it.checkpointId == checkpointId }) {
            "Checkpoint $checkpointId does not belong to this tour."
        }

        val normalizedNotes = normalizeOptionalNotes(notes)
        val visitedAt = if (status == CheckpointStatus.NOT_VISITED) null else now
        val updatedProgress = checkpointProgress.map { progress ->
            if (progress.checkpointId != checkpointId) {
                progress
            } else {
                TourCheckpointProgress(
                    checkpointId = progress.checkpointId,
                    status = status,
                    visitedAt = visitedAt,
                    notes = normalizedNotes,
                )
            }
        }

        return copy(
            checkpointProgress = updatedProgress,
            updatedAt = now,
        ).validated()
    }

    /**
     * Completes the tour after all checkpoints were visited.
     */
    fun endTour(now: Instant = Clock.System.now()): Tour {
        require(now >= createdAt) { "Tour updatedAt must not be earlier than createdAt." }
        require(status == TourStatus.IN_PROGRESS) { "Tour can only be ended from IN_PROGRESS status." }
        require(allCheckpointsVisited) { "Tour cannot be ended before all checkpoints are visited." }
        val currentStartedAt = requireNotNull(startedAt) { "Tour startedAt must be present while IN_PROGRESS." }
        require(now >= currentStartedAt) { "Tour end timestamp must not be earlier than startedAt." }

        return copy(
            status = TourStatus.COMPLETED,
            endedAt = now,
            updatedAt = now,
        ).validated()
    }

    /**
     * Cancels the tour while it is still non-terminal.
     */
    fun cancel(now: Instant = Clock.System.now()): Tour {
        require(now >= createdAt) { "Tour updatedAt must not be earlier than createdAt." }
        require(!status.isTerminal) { "Tour cannot be cancelled from terminal status $status." }
        startedAt?.let {
            require(now >= it) { "Tour cancellation timestamp must not be earlier than startedAt." }
        }

        return copy(
            status = TourStatus.CANCELLED,
            endedAt = if (startedAt != null) now else null,
            updatedAt = now,
        ).validated()
    }

    private fun copy(
        id: TourId = this.id,
        createdAt: Instant = this.createdAt,
        updatedAt: Instant = this.updatedAt,
        type: TourType = this.type,
        startedBy: UserId = this.startedBy,
        status: TourStatus = this.status,
        startedAt: Instant? = this.startedAt,
        endedAt: Instant? = this.endedAt,
        participants: Set<UserId> = this.participants,
        checkpointProgress: List<TourCheckpointProgress> = this.checkpointProgress,
    ): Tour = Tour(
        id = id,
        createdAt = createdAt,
        updatedAt = updatedAt,
        type = type,
        startedBy = startedBy,
        status = status,
        startedAt = startedAt,
        endedAt = endedAt,
        participants = participants,
        checkpointProgress = checkpointProgress,
    )

    private fun validated(): Tour {
        require(updatedAt >= createdAt) { "Tour updatedAt must not be earlier than createdAt." }
        require(participants.isNotEmpty()) { "Tour participants must not be empty." }
        require(startedBy in participants) { "Tour participants must include startedBy user." }
        require(checkpointProgress.isNotEmpty()) { "Tour checkpointProgress must not be empty." }

        val uniqueCheckpointCount = checkpointProgress.map { it.checkpointId }.toSet().size
        require(uniqueCheckpointCount == checkpointProgress.size) {
            "Tour checkpointProgress must not contain duplicate checkpointId values."
        }

        when (status) {
            TourStatus.NOT_STARTED -> {
                require(startedAt == null) { "Tour startedAt must be null when status is NOT_STARTED." }
                require(endedAt == null) { "Tour endedAt must be null when status is NOT_STARTED." }
            }

            TourStatus.IN_PROGRESS -> {
                require(startedAt != null) { "Tour startedAt must be present when status is IN_PROGRESS." }
                require(endedAt == null) { "Tour endedAt must be null when status is IN_PROGRESS." }
            }

            TourStatus.COMPLETED -> {
                require(startedAt != null) { "Tour startedAt must be present when status is COMPLETED." }
                require(endedAt != null) { "Tour endedAt must be present when status is COMPLETED." }
            }

            TourStatus.CANCELLED -> {
                if (endedAt != null) {
                    require(startedAt != null) {
                        "Tour startedAt must be present when cancelled tour has endedAt."
                    }
                }
            }
        }

        if (startedAt != null && endedAt != null) {
            require(endedAt >= startedAt) { "Tour endedAt must not be earlier than startedAt." }
        }

        return this
    }

    companion object {
        /**
         * Creates a new tour with generated identity and validated aggregate state.
         */
        fun create(
            type: TourType,
            startedBy: UserId,
            participants: Set<UserId> = setOf(startedBy),
            checkpointProgress: List<TourCheckpointProgress>,
            now: Instant = Clock.System.now(),
        ): Tour = Tour(
            id = TourId.generate(),
            createdAt = now,
            updatedAt = now,
            type = type,
            startedBy = startedBy,
            status = TourStatus.NOT_STARTED,
            startedAt = null,
            endedAt = null,
            participants = participants + startedBy,
            checkpointProgress = checkpointProgress,
        ).validated()

        /**
         * Reconstructs a persisted tour while enforcing aggregate invariants.
         */
        internal fun rehydrate(
            id: TourId,
            createdAt: Instant,
            updatedAt: Instant,
            type: TourType,
            startedBy: UserId,
            status: TourStatus,
            startedAt: Instant?,
            endedAt: Instant?,
            participants: Set<UserId>,
            checkpointProgress: List<TourCheckpointProgress>,
        ): Tour = Tour(
            id = id,
            createdAt = createdAt,
            updatedAt = updatedAt,
            type = type,
            startedBy = startedBy,
            status = status,
            startedAt = startedAt,
            endedAt = endedAt,
            participants = participants,
            checkpointProgress = checkpointProgress,
        ).validated()

        private fun normalizeOptionalNotes(notes: String?): String? {
            val normalized = notes?.trim() ?: return null
            require(normalized.isNotBlank()) { "Tour checkpoint notes must not be blank when provided." }
            return normalized
        }
    }
}
