package nl.freek.vgwm.data.table

import nl.freek.vgwm.domain.value.UserRole
import org.jetbrains.exposed.v1.core.dao.id.IdTable
import org.jetbrains.exposed.v1.datetime.timestamp

/**
 * Persistence schema for application users.
 */
object UsersTable : IdTable<String>("users") {
    override val id = varchar("id", 36).entityId()
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")
    val name = varchar("name", 255)
    val email = varchar("email", 320).uniqueIndex()
    val role = enumerationByName("role", 32, UserRole::class)
    val passwordHash = varchar("password_hash", 255)

    override val primaryKey = PrimaryKey(id)
}
