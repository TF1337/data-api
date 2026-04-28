package nl.freek.vgwm.data.table

import org.jetbrains.exposed.v1.core.Table

/**
 * Singleton-style table for deployment-specific company branding and labels.
 */
object CompanyInfoTable : Table("company_info") {
    val id = varchar("id", 64)
    val companyName = varchar("company_name", 255)
    val displayName = varchar("display_name", 255)
    val primaryColorHex = varchar("primary_color_hex", 7).nullable()
    val secondaryColorHex = varchar("secondary_color_hex", 7).nullable()
    val defaultLanguage = varchar("default_language", 8)

    override val primaryKey = PrimaryKey(id)
}
