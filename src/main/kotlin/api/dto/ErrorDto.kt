package nl.freek.vgwm.api.dto

import kotlinx.serialization.Serializable

@Serializable
data class ErrorResponseDto(
    val message: String,
    val code: String? = null,
)
