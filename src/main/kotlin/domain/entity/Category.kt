package nl.freek.vgwm.domain.entity

import nl.freek.vgwm.domain.base.Entity
import nl.freek.vgwm.domain.id.CategoryId
import nl.freek.vgwm.domain.value.Translations

/**
 * Category reference entry used to classify observations and checkpoint applicability.
 */
class Category private constructor(
    override val id: CategoryId,
    val code: String,
    val displayName: Translations,
    val colorHex: String?,
) : Entity() {
    companion object {
        private val colorHexRegex = Regex("^#(?:[0-9A-Fa-f]{3}|[0-9A-Fa-f]{6})$")

        /**
         * Creates a new category with generated identity and normalized, validated fields.
         */
        fun create(
            code: String,
            displayName: Translations,
            colorHex: String? = null,
        ): Category = Category(
            id = CategoryId.generate(),
            code = code.trim().uppercase(),
            displayName = displayName,
            colorHex = colorHex?.trim(),
        ).validated()

        /**
         * Reconstructs a persisted category while enforcing domain invariants.
         */
        internal fun rehydrate(
            id: CategoryId,
            code: String,
            displayName: Translations,
            colorHex: String?,
        ): Category = Category(
            id = id,
            code = code.trim().uppercase(),
            displayName = displayName,
            colorHex = colorHex?.trim(),
        ).validated()
    }

    private fun validated(): Category {
        require(code.isNotBlank()) { "Category code must not be blank." }
        require(code == code.uppercase()) { "Category code must be uppercase." }
        colorHex?.let {
            require(it.isNotBlank()) { "Category colorHex must not be blank when provided." }
            require(colorHexRegex.matches(it)) {
                "Category colorHex must match #RGB or #RRGGBB format when provided."
            }
        }
        return this
    }
}
