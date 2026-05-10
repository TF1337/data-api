package nl.freek.vgwm.data.repository

import nl.freek.vgwm.data.mapper.CompanyInfoMapper
import nl.freek.vgwm.data.table.CompanyInfoTable
import nl.freek.vgwm.domain.repository.CompanyInfo
import nl.freek.vgwm.domain.repository.CompanyInfoRepository
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.experimental.newSuspendedTransaction

/**
 * Exposed-backed implementation of [CompanyInfoRepository].
 */
class ExposedCompanyInfoRepository : CompanyInfoRepository {
    override suspend fun findCurrent(): CompanyInfo? = newSuspendedTransaction {
        CompanyInfoTable
            .selectAll()
            .firstOrNull()
            ?.let(CompanyInfoMapper::toDomain)
    }
}
