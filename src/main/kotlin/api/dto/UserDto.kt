package nl.freek.vgwm.api.dto

import kotlinx.serialization.Serializable

@Serializable
data class UserDto(
    val id: String,
    val name: String,
    val email: String,
    val role: String,
    val createdAt: String,
    val updatedAt: String,
)
