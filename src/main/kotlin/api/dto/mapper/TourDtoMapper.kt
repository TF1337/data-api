package nl.freek.vgwm.api.dto.mapper

import nl.freek.vgwm.api.dto.TourCheckpointProgressDto
import nl.freek.vgwm.api.dto.TourDto
import nl.freek.vgwm.domain.entity.Tour
import nl.freek.vgwm.domain.value.TourCheckpointProgress

fun TourCheckpointProgress.toDto(): TourCheckpointProgressDto = TourCheckpointProgressDto(
    checkpointId = checkpointId.value,
    status = status.name,
    visitedAt = visitedAt?.toString(),
    notes = notes,
    isVisited = isVisited,
)

fun Tour.toDto(): TourDto = TourDto(
    id = id.value,
    type = type.name,
    status = status.name,
    startedBy = startedBy.value,
    participants = participants.map { it.value }.sorted(),
    checkpointProgress = checkpointProgress.map { it.toDto() },
    startedAt = startedAt?.toString(),
    endedAt = endedAt?.toString(),
    createdAt = createdAt.toString(),
    updatedAt = updatedAt.toString(),
)