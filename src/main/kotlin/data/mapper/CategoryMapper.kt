package nl.freek.vgwm.data.mapper

import nl.freek.vgwm.data.entity.CategoryEntity
import nl.freek.vgwm.domain.entity.Category
import nl.freek.vgwm.domain.id.CategoryId
import nl.freek.vgwm.domain.value.Translations

/**
 * Maps between [CategoryEntity] and domain [Category].
 */
object CategoryMapper {
    fun toDomain(entity: CategoryEntity): Category = Category.rehydrate(
        id = CategoryId(entity.id.value),
        code = entity.code,
        displayName = Translations(en = entity.displayNameEn, nl = entity.displayNameNl),
        colorHex = entity.colorHex,
    )

    fun applyToEntity(domain: Category, entity: CategoryEntity) {
        entity.code = domain.code
        entity.displayNameEn = domain.displayName.en
        entity.displayNameNl = domain.displayName.nl
        entity.colorHex = domain.colorHex
    }
}