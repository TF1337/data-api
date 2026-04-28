package nl.freek.vgwm.api.dto

import kotlinx.serialization.Serializable

@Serializable
data class TourCheckpointProgressDto(
    val checkpointId: String,
    val status: String,
    val visitedAt: String?,
    val notes: String?,
    val isVisited: Boolean,
)

@Serializable
data class TourDto(
    val id: String,
    val type: String,
    val status: String,
    val startedBy: String,
    val participants: List<String>,
    val checkpointProgress: List<TourCheckpointProgressDto>,
    val startedAt: String?,
    val endedAt: String?,
    val createdAt: String,
    val updatedAt: String,
)

@Serializable
data class CreateTourRequestDto(
    val type: String,
    val startedBy: String,
    val participants: List<String> = emptyList(),
)

@Serializable
data class UpdateTourCheckpointProgressRequestDto(
    val checkpointId: String,
    val status: String,
    val visitedAt: String? = null,
    val notes: String? = null,
)

@Serializable
data class AddTourParticipantRequestDto(
    val userId: String,
)