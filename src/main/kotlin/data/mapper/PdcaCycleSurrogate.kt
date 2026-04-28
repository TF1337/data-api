package nl.freek.vgwm.data.mapper

import kotlinx.serialization.Serializable
import nl.freek.vgwm.domain.value.PdcaCycle

/**
 * Serializable persistence surrogate for [PdcaCycle].
 */
@Serializable
data class PdcaCycleSurrogate(
    val plan: String,
    val doStep: String,
    val check: String,
    val act: String,
) {
    fun toDomain(): PdcaCycle = PdcaCycle(
        plan = plan,
        doStep = doStep,
        check = check,
        act = act,
    )

    companion object {
        fun fromDomain(domain: PdcaCycle): PdcaCycleSurrogate = PdcaCycleSurrogate(
            plan = domain.plan,
            doStep = domain.doStep,
            check = domain.check,
            act = domain.act,
        )
    }
}