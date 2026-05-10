package nl.freek.vgwm.data.mapper

import nl.freek.vgwm.data.table.CompanyInfoTable
import nl.freek.vgwm.domain.repository.CompanyInfo
import org.jetbrains.exposed.v1.core.ResultRow

/**
 * Maps persistence rows to company-info read models.
 */
object CompanyInfoMapper {
    fun toDomain(row: ResultRow): CompanyInfo = CompanyInfo(
        companyName = row[CompanyInfoTable.companyName],
        displayName = row[CompanyInfoTable.displayName],
        primaryColorHex = row[CompanyInfoTable.primaryColorHex],
        secondaryColorHex = row[CompanyInfoTable.secondaryColorHex],
        defaultLanguage = row[CompanyInfoTable.defaultLanguage],
    )
}
