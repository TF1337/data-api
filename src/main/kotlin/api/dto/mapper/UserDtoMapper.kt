package nl.freek.vgwm.api.dto.mapper

import nl.freek.vgwm.api.dto.UserDto
import nl.freek.vgwm.domain.entity.User

fun User.toDto(): UserDto = UserDto(
    id = id.value,
    name = name,
    email = email,
    role = role.name,
    createdAt = createdAt.toString(),
    updatedAt = updatedAt.toString(),
)
