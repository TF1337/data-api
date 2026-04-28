package nl.freek.vgwm.data.external

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.UUID
import nl.freek.vgwm.domain.service.PhotoStorage

/**
 * Local file-system implementation of [PhotoStorage].
 *
 * Stored photo references are relative file names under a fixed upload directory.
 */
class LocalFilePhotoStorage(
    uploadDir: String,
) : PhotoStorage {
    private val baseDirectory: Path = Paths.get(uploadDir).toAbsolutePath().normalize()

    init {
        Files.createDirectories(baseDirectory)
    }

    /**
     * Stores photo bytes under a generated file name and returns the relative storage reference.
     */
    override suspend fun save(bytes: ByteArray): String {
        require(bytes.isNotEmpty()) { "Photo bytes must not be empty." }

        val fileName = generateFileName()
        val targetPath = resolveSafe(fileName)
        Files.write(targetPath, bytes)
        return fileName
    }

    /**
     * Loads photo bytes for a previously returned storage reference.
     */
    override suspend fun load(path: String): ByteArray? {
        val targetPath = resolveSafe(path)
        if (!Files.exists(targetPath)) {
            return null
        }
        return Files.readAllBytes(targetPath)
    }

    /**
     * Deletes a stored photo by storage reference.
     *
     * @return true when the file was deleted, false when the file does not exist.
     */
    override suspend fun delete(path: String): Boolean {
        val targetPath = resolveSafe(path)
        if (!Files.exists(targetPath)) {
            return false
        }
        return Files.deleteIfExists(targetPath)
    }

    /**
     * Resolves [path] under the configured upload directory and rejects traversal attempts.
     */
    private fun resolveSafe(path: String): Path {
        require(path.isNotBlank()) { "Photo storage path must not be blank." }

        val resolved = baseDirectory.resolve(path).normalize()
        require(resolved.startsWith(baseDirectory)) {
            "Photo storage path escapes the upload directory."
        }
        return resolved
    }

    /**
     * Generates a random storage file name.
     */
    private fun generateFileName(): String = "${UUID.randomUUID()}.bin"
}
