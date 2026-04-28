package nl.freek.vgwm.data.table

import org.jetbrains.exposed.v1.core.dao.id.IdTable

/**
 * Persistence schema for observation categories.
 */
object CategoriesTable : IdTable<String>("categories") {
    override val id = varchar("id", 36).entityId()
    val code = varchar("code", 32).uniqueIndex()
    val displayNameEn = varchar("display_name_en", 255)
    val displayNameNl = varchar("display_name_nl", 255)
    val colorHex = varchar("color_hex", 7).nullable()

    override val primaryKey = PrimaryKey(id)
}
