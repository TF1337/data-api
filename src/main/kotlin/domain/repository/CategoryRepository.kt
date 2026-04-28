package nl.freek.vgwm.domain.repository

import nl.freek.vgwm.domain.entity.Category
import nl.freek.vgwm.domain.id.CategoryId

/**
 * Repository contract for querying category reference data.
 */
interface CategoryRepository {
    /**
     * Returns all configured categories.
     */
    suspend fun findAll(): List<Category>

    /**
     * Finds a category by id.
     */
    suspend fun findById(id: CategoryId): Category?

    /**
     * Finds a category by its domain code.
     */
    suspend fun findByCode(code: String): Category?
}
