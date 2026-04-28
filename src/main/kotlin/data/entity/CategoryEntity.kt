package nl.freek.vgwm.data.entity

import nl.freek.vgwm.data.table.CategoriesTable
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.Entity
import org.jetbrains.exposed.v1.dao.EntityClass

/**
 * Exposed DAO entity for rows stored in [CategoriesTable].
 */
class CategoryEntity(id: EntityID<String>) : Entity<String>(id) {
    companion object : EntityClass<String, CategoryEntity>(CategoriesTable)

    var code by CategoriesTable.code
    var displayNameEn by CategoriesTable.displayNameEn
    var displayNameNl by CategoriesTable.displayNameNl
    var colorHex by CategoriesTable.colorHex
}
