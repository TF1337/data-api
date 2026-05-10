package nl.freek.vgwm.domain.repository

import nl.freek.vgwm.domain.entity.User
import nl.freek.vgwm.domain.id.UserId

/**
 * Repository contract for managing users.
 */
interface UserRepository {
    /**
     * Lists all users.
     */
    suspend fun findAll(): List<User>

    /**
     * Finds a user by unique identity.
     */
    suspend fun findById(id: UserId): User?

    /**
     * Finds a user by email address.
     */
    suspend fun findByEmail(email: String): User?

    /**
     * Persists a user and returns the saved domain state.
     */
    suspend fun save(user: User): User

    /**
     * Deletes a user by id.
     *
     * @return true when a user was deleted, false when no matching record existed.
     */
    suspend fun delete(id: UserId): Boolean
}
