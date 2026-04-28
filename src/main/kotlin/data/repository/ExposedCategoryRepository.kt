package nl.freek.vgwm.data.repository

import nl.freek.vgwm.data.entity.CategoryEntity
import nl.freek.vgwm.data.mapper.CategoryMapper
import nl.freek.vgwm.data.table.CategoriesTable
import nl.freek.vgwm.domain.entity.Category
import nl.freek.vgwm.domain.id.CategoryId
import nl.freek.vgwm.domain.repository.CategoryRepository
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.jdbc.transactions.experimental.newSuspendedTransaction

/**
 * Exposed-backed implementation of [CategoryRepository].
 */
class ExposedCategoryRepository : CategoryRepository {

    override suspend fun findAll(): List<Category> = newSuspendedTransaction {
        CategoryEntity.all()
            .orderBy(CategoriesTable.code to SortOrder.ASC)
            .map(CategoryMapper::toDomain)
    }

    override suspend fun findById(id: CategoryId): Category? = newSuspendedTransaction {
        CategoryEntity.findById(id.value)?.let(CategoryMapper::toDomain)
    }

    override suspend fun findByCode(code: String): Category? = newSuspendedTransaction {
        val normalizedCode = code.trim().uppercase()
        CategoryEntity.find { CategoriesTable.code eq normalizedCode }
            .firstOrNull()
            ?.let(CategoryMapper::toDomain)
    }
}
