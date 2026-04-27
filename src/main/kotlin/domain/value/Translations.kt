package nl.freek.vgwm.domain.value

/**
 * Two-language text holder used for user-facing labels.
 */
data class Translations(
    val en: String,
    val nl: String,
) {
    init {
        require(en.isNotBlank()) { "English translation must not be blank." }
        require(nl.isNotBlank()) { "Dutch translation must not be blank." }
    }

    /**
     * Returns the best matching translation for the given language code.
     * Defaults to English for unknown or blank values.
     */
    fun forLanguage(code: String): String = when (code.lowercase()) {
        "nl" -> nl
        "en" -> en
        else -> en
    }
}
