package nl.freek.vgwm.api.dto

import kotlinx.serialization.Serializable

@Serializable
data class TranslationsDto(
    val en: String,
    val nl: String,
)

@Serializable
data class CategoryDto(
    val id: String,
    val code: String,
    val displayName: TranslationsDto,
    val colorHex: String?,
)
