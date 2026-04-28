package nl.freek.vgwm.api.dto

import kotlinx.serialization.Serializable

@Serializable
data class LocationDto(
    val id: String,
    val name: String,
    val parentLocationId: String?,
    val orderIndex: Int,
    val attentionNote: String?,
    val isTopLevel: Boolean,
)
