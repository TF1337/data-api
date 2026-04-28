package nl.freek.vgwm.data.mapper

import nl.freek.vgwm.data.entity.UserEntity
import nl.freek.vgwm.domain.entity.User
import nl.freek.vgwm.domain.id.UserId
import nl.freek.vgwm.domain.value.PasswordHash

/**
 * Maps between [UserEntity] and domain [User].
 */
object UserMapper {
    fun toDomain(entity: UserEntity): User = User.rehydrate(
        id = UserId(entity.id.value),
        createdAt = entity.createdAt.toDomainInstant(),
        updatedAt = entity.updatedAt.toDomainInstant(),
        name = entity.name,
        email = entity.email,
        role = entity.role,
        passwordHash = PasswordHash(entity.passwordHash),
    )

    fun applyToEntity(domain: User, entity: UserEntity) {
        entity.createdAt = domain.createdAt.toPersistenceInstant()
        entity.updatedAt = domain.updatedAt.toPersistenceInstant()
        entity.name = domain.name
        entity.email = domain.email
        entity.role = domain.role
        entity.passwordHash = domain.passwordHash.value
    }
}

private fun kotlin.time.Instant.toDomainInstant(): kotlinx.datetime.Instant =
    kotlinx.datetime.Instant.parse(toString())

private fun kotlinx.datetime.Instant.toPersistenceInstant(): kotlin.time.Instant =
    kotlin.time.Instant.parse(toString())