package nl.freek.vgwm.domain.value

/**
 * Pre-hashed password string stored by the domain.
 *
 * This type validates only hash shape; hashing itself is handled by a hasher strategy.
 */
@JvmInline
value class PasswordHash(val value: String) {
    init {
        require(value.isNotBlank()) { "Password hash must not be blank." }
        require(
            value.startsWith("$2a$") || value.startsWith("$2b$") || value.startsWith("$2y$"),
        ) {
            "Password hash must start with a supported bcrypt prefix ($2a$, $2b$, or $2y$)."
        }
    }
}
