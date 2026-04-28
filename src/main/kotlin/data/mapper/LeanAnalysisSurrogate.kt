package nl.freek.vgwm.data.mapper

import kotlinx.serialization.Serializable
import nl.freek.vgwm.domain.value.LeanAnalysis

/**
 * Serializable persistence surrogate for [LeanAnalysis].
 */
@Serializable
data class LeanAnalysisSurrogate(
    val wasteType: String,
    val sixSCategory: String,
    val threeM: String,
    val processFocus: String,
    val gembaObservation: String,
    val pdca: PdcaCycleSurrogate,
) {
    fun toDomain(): LeanAnalysis = LeanAnalysis(
        wasteType = wasteType,
        sixSCategory = sixSCategory,
        threeM = threeM,
        processFocus = processFocus,
        gembaObservation = gembaObservation,
        pdca = pdca.toDomain(),
    )

    companion object {
        fun fromDomain(domain: LeanAnalysis): LeanAnalysisSurrogate = LeanAnalysisSurrogate(
            wasteType = domain.wasteType,
            sixSCategory = domain.sixSCategory,
            threeM = domain.threeM,
            processFocus = domain.processFocus,
            gembaObservation = domain.gembaObservation,
            pdca = PdcaCycleSurrogate.fromDomain(domain.pdca),
        )
    }
}