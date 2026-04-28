package nl.freek.vgwm.api.dto.mapper

import nl.freek.vgwm.api.dto.AuthTokenResponseDto
import nl.freek.vgwm.domain.service.AuthToken

fun AuthToken.toDto(): AuthTokenResponseDto = AuthTokenResponseDto(
    token = token,
    expiresAt = expiresAt.toString(),
)
