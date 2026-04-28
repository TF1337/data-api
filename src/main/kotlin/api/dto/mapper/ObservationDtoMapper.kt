package nl.freek.vgwm.api.dto.mapper

import nl.freek.vgwm.api.dto.ObservationDto
import nl.freek.vgwm.domain.entity.Observation

fun Observation.toDto(): ObservationDto = ObservationDto(
    id = id.value,
    tourId = tourId?.value,
    checkpointId = checkpointId.value,
    observerId = observerId.value,
    assigneeId = assigneeId?.value,
    categoryId = categoryId.value,
    observationType = observationType.name,
    description = description,
    priority = priority.code,
    status = status.name,
    deadline = deadline.toString(),
    photoPath = photoPath,
    externalRef = externalRef,
    aiAnalysis = aiAnalysis?.toDto(),
    createdAt = createdAt.toString(),
    updatedAt = updatedAt.toString(),
)