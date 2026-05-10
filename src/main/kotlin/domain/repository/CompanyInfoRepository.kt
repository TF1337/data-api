package nl.freek.vgwm.domain.repository

/**
 * Read model for deployment-specific company information.
 */
data class CompanyInfo(
    val companyName: String,
    val displayName: String,
    val primaryColorHex: String?,
    val secondaryColorHex: String?,
    val defaultLanguage: String,
)

/**
 * Repository contract for reading the current company-info singleton row.
 */
interface CompanyInfoRepository {
    /**
     * Finds the current company info singleton row.
     */
    suspend fun findCurrent(): CompanyInfo?
}
