package nl.freek.vgwm.api.dto.mapper

import nl.freek.vgwm.api.dto.CategoryDto
import nl.freek.vgwm.api.dto.CheckpointDto
import nl.freek.vgwm.api.dto.LocationDto
import nl.freek.vgwm.api.dto.TranslationsDto
import nl.freek.vgwm.domain.entity.Category
import nl.freek.vgwm.domain.entity.Checkpoint
import nl.freek.vgwm.domain.entity.Location
import nl.freek.vgwm.domain.value.Translations

fun Location.toDto(): LocationDto = LocationDto(
    id = id.value,
    name = name,
    parentLocationId = parentLocationId?.value,
    orderIndex = orderIndex,
    attentionNote = attentionNote,
    isTopLevel = isTopLevel,
)

fun Category.toDto(): CategoryDto = CategoryDto(
    id = id.value,
    code = code,
    displayName = displayName.toDto(),
    colorHex = colorHex,
)

fun Translations.toDto(): TranslationsDto = TranslationsDto(
    en = en,
    nl = nl,
)

fun Checkpoint.toDto(): CheckpointDto = CheckpointDto(
    id = id.value,
    locationId = locationId.value,
    name = name,
    attentionNote = attentionNote,
    applicableCategoryIds = applicableCategories
        .map { it.value }
        .sorted(),
)
