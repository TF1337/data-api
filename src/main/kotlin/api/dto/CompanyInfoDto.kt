package nl.freek.vgwm.api.dto

import kotlinx.serialization.Serializable

@Serializable
data class CompanyInfoDto(
    val companyName: String,
    val displayName: String,
    val primaryColorHex: String?,
    val secondaryColorHex: String?,
    val defaultLanguage: String,
)
