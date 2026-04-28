package nl.freek.vgwm.data.mapper

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import nl.freek.vgwm.domain.value.AiAnalysis

/**
 * Serializable persistence surrogate for [AiAnalysis].
 */
@Serializable
data class AiAnalysisSurrogate(
    val photoDescription: String?,
    val fiveW1H: FiveW1HSurrogate?,
    val rootCauseAnalysis: RootCauseAnalysisSurrogate?,
    val leanAnalysis: LeanAnalysisSurrogate?,
    val generatedAt: Instant,
) {
    fun toDomain(): AiAnalysis = AiAnalysis(
        photoDescription = photoDescription,
        fiveW1H = fiveW1H?.toDomain(),
        rootCauseAnalysis = rootCauseAnalysis?.toDomain(),
        leanAnalysis = leanAnalysis?.toDomain(),
        generatedAt = generatedAt,
    )

    companion object {
        fun fromDomain(domain: AiAnalysis): AiAnalysisSurrogate = AiAnalysisSurrogate(
            photoDescription = domain.photoDescription,
            fiveW1H = domain.fiveW1H?.let(FiveW1HSurrogate::fromDomain),
            rootCauseAnalysis = domain.rootCauseAnalysis?.let(RootCauseAnalysisSurrogate::fromDomain),
            leanAnalysis = domain.leanAnalysis?.let(LeanAnalysisSurrogate::fromDomain),
            generatedAt = domain.generatedAt,
        )
    }
}