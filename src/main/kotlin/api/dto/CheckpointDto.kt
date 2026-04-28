package nl.freek.vgwm.api.dto

import kotlinx.serialization.Serializable

@Serializable
data class CheckpointDto(
    val id: String,
    val locationId: String,
    val name: String,
    val attentionNote: String?,
    val applicableCategoryIds: List<String>,
)
