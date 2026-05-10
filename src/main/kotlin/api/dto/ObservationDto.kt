package nl.freek.vgwm.api.dto

import kotlinx.serialization.Serializable

@Serializable
data class ObservationDto(
    val id: String,
    val tourId: String?,
    val checkpointId: String,
    val observerId: String,
    val assigneeId: String?,
    val categoryId: String,
    val observationType: String,
    val description: String,
    val priority: String,
    val status: String,
    val deadline: String,
    val photoPath: String?,
    val externalRef: String?,
    val aiAnalysis: AiAnalysisDto?,
    val createdAt: String,
    val updatedAt: String,
)

@Serializable
data class CreateObservationRequestDto(
    val tourId: String? = null,
    val checkpointId: String,
    val observerId: String,
    val assigneeId: String? = null,
    val categoryId: String,
    val observationType: String,
    val description: String,
    val priority: String,
    val photoBase64: String? = null,
    val aiContext: ObservationAiContextDto? = null,
    val requestAiAnalysis: Boolean = false,
    val aiAnalysis: AiAnalysisDto? = null,
)

@Serializable
data class ObservationAiContextDto(
    val checkpointLabel: String,
    val locationLabel: String,
    val observerLabel: String,
    val assigneeLabel: String?,
    val observationType: String,
    val description: String,
    val observedAt: String,
)

@Serializable
data class CreateObservationDraftAnalysisRequestDto(
    val photoBase64: String? = null,
    val aiContext: ObservationAiContextDto? = null,
)

@Serializable
data class UpdateObservationRequestDto(
    val description: String? = null,
    val assigneeId: String? = null,
    val externalRef: String? = null,
    val aiAnalysis: AiAnalysisDto? = null,
    val status: String? = null,
)