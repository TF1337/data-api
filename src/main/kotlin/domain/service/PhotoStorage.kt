package nl.freek.vgwm.domain.service

/**
 * Strategy contract for photo binary persistence.
 */
interface PhotoStorage {
    /**
     * Stores raw photo bytes and returns a storage path identifier.
     */
    suspend fun save(bytes: ByteArray): String

    /**
     * Loads previously stored photo bytes for the given path.
     */
    suspend fun load(path: String): ByteArray?

    /**
     * Deletes the stored photo for the given path.
     *
     * @return true when a file was deleted, false when the path was not found.
     */
    suspend fun delete(path: String): Boolean
}
