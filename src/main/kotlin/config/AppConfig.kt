package nl.freek.vgwm.config

/**
 * Immutable application configuration loaded from environment variables.
 */
data class AppConfig(
    val database: DatabaseSettings,
    val jwt: JwtSettings,
    val chatbotApiUrl: String,
    val uploadDir: String,
    val port: Int,
) {

    /**
     * Database-related runtime settings.
     */
    data class DatabaseSettings(
        val url: String,
        val user: String,
        val password: String,
    )

    /**
     * JWT-related runtime settings.
     */
    data class JwtSettings(
        val secret: String,
        val issuer: String,
        val audience: String,
        val expirationHours: Long,
    )

    companion object {
        /**
         * Loads configuration from environment variables.
         *
         * This function is intentionally explicit and must be called during application startup.
         * Missing required values and invalid numeric values fail fast with clear errors.
         */
        fun fromEnvironment(env: Map<String, String> = System.getenv()): AppConfig {
            val database = DatabaseSettings(
                url = env.getOrDefault("DB_URL", "jdbc:sqlite:./vgwm.db"),
                user = env.getOrDefault("DB_USER", ""),
                password = env.getOrDefault("DB_PASSWORD", ""),
            )

            val jwt = JwtSettings(
                secret = requiredNonBlank(env, "JWT_SECRET"),
                issuer = env.getOrDefault("JWT_ISSUER", "vgwm-data-api"),
                audience = env.getOrDefault("JWT_AUDIENCE", "vgwm-android"),
                expirationHours = parsePositiveLong(
                    value = env["JWT_EXPIRATION_HOURS"],
                    key = "JWT_EXPIRATION_HOURS",
                    defaultValue = 24L,
                ),
            )

            return AppConfig(
                database = database,
                jwt = jwt,
                chatbotApiUrl = env.getOrDefault("CHATBOT_API_URL", "http://127.0.0.1:8081"),
                uploadDir = env.getOrDefault("UPLOAD_DIR", "./uploads"),
                port = parsePositiveInt(
                    value = env["PORT"],
                    key = "PORT",
                    defaultValue = 8080,
                ),
            )
        }

        private fun requiredNonBlank(env: Map<String, String>, key: String): String {
            val value = env[key]
            require(!value.isNullOrBlank()) {
                "Missing required environment variable: $key"
            }
            return value
        }

        private fun parsePositiveLong(value: String?, key: String, defaultValue: Long): Long {
            if (value.isNullOrBlank()) return defaultValue
            return value.toLongOrNull()?.takeIf { it > 0L }
                ?: throw IllegalArgumentException(
                    "Invalid environment variable $key: '$value'. Expected a positive integer.",
                )
        }

        private fun parsePositiveInt(value: String?, key: String, defaultValue: Int): Int {
            if (value.isNullOrBlank()) return defaultValue
            return value.toIntOrNull()?.takeIf { it > 0 }
                ?: throw IllegalArgumentException(
                    "Invalid environment variable $key: '$value'. Expected a positive integer.",
                )
        }
    }
}
