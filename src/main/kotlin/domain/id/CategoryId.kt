package nl.freek.vgwm.domain.id

import java.util.UUID

/**
 * Strongly typed identifier for a category.
 */
@JvmInline
value class CategoryId(val value: String) {
    init {
        require(value.isNotBlank()) { "CategoryId value must not be blank." }
    }

    companion object {
        /**
         * Generates a new category identifier backed by a random UUID.
         */
        fun generate(): CategoryId = CategoryId(UUID.randomUUID().toString())
    }
}
