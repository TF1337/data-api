package nl.freek.vgwm.domain.value

import kotlin.time.Duration.Companion.days
import kotlinx.datetime.Instant

/**
 * Priority level with behavior for deadline calculation and localized labels.
 */
sealed class Priority(
    val code: String,
    val rank: Int,
) {
    /**
     * Calculates the expected deadline from the creation timestamp for this priority level.
     */
    abstract fun deadlineFrom(createdAt: Instant): Instant

    /**
     * Human-readable localized label for this priority.
     */
    abstract val displayName: Translations

    /**
     * Acute risk that should be addressed within one day.
     */
    data object Acute : Priority(code = "ACUTE", rank = 0) {
        override fun deadlineFrom(createdAt: Instant): Instant = createdAt + 1.days

        override val displayName: Translations = Translations(en = "Acute", nl = "Acuut")
    }

    /**
     * High priority that should be addressed within one week.
     */
    data object High : Priority(code = "HIGH", rank = 1) {
        override fun deadlineFrom(createdAt: Instant): Instant = createdAt + 7.days

        override val displayName: Translations = Translations(en = "High", nl = "Hoog")
    }

    /**
     * Normal priority that should be addressed within thirty days.
     */
    data object Normal : Priority(code = "NORMAL", rank = 2) {
        override fun deadlineFrom(createdAt: Instant): Instant = createdAt + 30.days

        override val displayName: Translations = Translations(en = "Normal", nl = "Normaal")
    }

    /**
     * Low priority that should be addressed within ninety days.
     */
    data object Low : Priority(code = "LOW", rank = 3) {
        override fun deadlineFrom(createdAt: Instant): Instant = createdAt + 90.days

        override val displayName: Translations = Translations(en = "Low", nl = "Laag")
    }

    companion object {
        /**
         * Stable list of all supported priorities ordered by severity.
         */
        val all: List<Priority> = listOf(Acute, High, Normal, Low)

        /**
         * Parses a persisted priority code.
         *
         * @throws IllegalArgumentException when the code is unknown.
         */
        fun fromCode(code: String): Priority = all.firstOrNull { it.code == code }
            ?: throw IllegalArgumentException("Unknown priority code: $code")
    }
}
