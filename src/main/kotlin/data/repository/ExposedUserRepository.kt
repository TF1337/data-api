package nl.freek.vgwm.data.repository

import nl.freek.vgwm.data.entity.UserEntity
import nl.freek.vgwm.data.mapper.UserMapper
import nl.freek.vgwm.data.table.UsersTable
import nl.freek.vgwm.domain.entity.User
import nl.freek.vgwm.domain.id.UserId
import nl.freek.vgwm.domain.repository.UserRepository
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.transactions.experimental.newSuspendedTransaction

/**
 * Exposed-backed implementation of [UserRepository].
 */
class ExposedUserRepository : UserRepository {

    override suspend fun findById(id: UserId): User? = newSuspendedTransaction {
        UserEntity.findById(id.value)?.let(UserMapper::toDomain)
    }

    override suspend fun findByEmail(email: String): User? = newSuspendedTransaction {
        val normalizedEmail = normalizeEmail(email)
        UserEntity.find { UsersTable.email eq normalizedEmail }
            .firstOrNull()
            ?.let(UserMapper::toDomain)
    }

    override suspend fun save(user: User): User = newSuspendedTransaction {
        val existing = UserEntity.findById(user.id.value)
        val entity = existing ?: UserEntity.new(user.id.value) {}

        UserMapper.applyToEntity(user, entity)
        UserMapper.toDomain(entity)
    }

    override suspend fun delete(id: UserId): Boolean = newSuspendedTransaction {
        val entity = UserEntity.findById(id.value) ?: return@newSuspendedTransaction false
        entity.delete()
        true
    }

    /**
     * Normalizes user email for consistent repository lookups.
     */
    private fun normalizeEmail(email: String): String = email.trim().lowercase()
}
