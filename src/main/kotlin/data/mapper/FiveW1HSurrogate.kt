package nl.freek.vgwm.data.mapper

import kotlinx.serialization.Serializable
import nl.freek.vgwm.domain.value.FiveW1H

/**
 * Serializable persistence surrogate for [FiveW1H].
 */
@Serializable
data class FiveW1HSurrogate(
    val who: String,
    val what: String,
    val where: String,
    val whenText: String,
    val why: String,
    val how: String,
) {
    fun toDomain(): FiveW1H = FiveW1H(
        who = who,
        what = what,
        where = where,
        whenText = whenText,
        why = why,
        how = how,
    )

    companion object {
        fun fromDomain(domain: FiveW1H): FiveW1HSurrogate = FiveW1HSurrogate(
            who = domain.who,
            what = domain.what,
            where = domain.where,
            whenText = domain.whenText,
            why = domain.why,
            how = domain.how,
        )
    }
}