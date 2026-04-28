package nl.freek.vgwm.data.mapper

import kotlinx.serialization.Serializable
import nl.freek.vgwm.domain.value.RootCauseAnalysis

/**
 * Serializable persistence surrogate for [RootCauseAnalysis].
 */
@Serializable
data class RootCauseAnalysisSurrogate(
    val why1: String,
    val why2: String,
    val why3: String,
    val why4: String,
    val why5: String,
    val rootCause: String,
    val recommendedMeasure: String,
) {
    fun toDomain(): RootCauseAnalysis = RootCauseAnalysis(
        why1 = why1,
        why2 = why2,
        why3 = why3,
        why4 = why4,
        why5 = why5,
        rootCause = rootCause,
        recommendedMeasure = recommendedMeasure,
    )

    companion object {
        fun fromDomain(domain: RootCauseAnalysis): RootCauseAnalysisSurrogate = RootCauseAnalysisSurrogate(
            why1 = domain.why1,
            why2 = domain.why2,
            why3 = domain.why3,
            why4 = domain.why4,
            why5 = domain.why5,
            rootCause = domain.rootCause,
            recommendedMeasure = domain.recommendedMeasure,
        )
    }
}