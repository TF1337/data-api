package nl.freek.vgwm.api.dto

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequestDto(
    val email: String,
    val password: String,
)

@Serializable
data class AuthTokenResponseDto(
    val token: String,
    val expiresAt: String,
)
