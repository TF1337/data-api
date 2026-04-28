package nl.freek.vgwm.data.entity

import nl.freek.vgwm.data.table.UsersTable
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.Entity
import org.jetbrains.exposed.v1.dao.EntityClass

/**
 * Exposed DAO entity for rows stored in [UsersTable].
 */
class UserEntity(id: EntityID<String>) : Entity<String>(id) {
    companion object : EntityClass<String, UserEntity>(UsersTable)

    var createdAt by UsersTable.createdAt
    var updatedAt by UsersTable.updatedAt
    var name by UsersTable.name
    var email by UsersTable.email
    var role by UsersTable.role
    var passwordHash by UsersTable.passwordHash
}
