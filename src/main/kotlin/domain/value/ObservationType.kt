package nl.freek.vgwm.domain.value

/**
 * Classification of an observation event.
 */
enum class ObservationType {
    UNSAFE,
    NEAR_MISS,
    IMPROVEMENT,
    GOOD_EXAMPLE,
    GMP_DEVIATION,
}
