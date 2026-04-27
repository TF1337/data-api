package nl.freek.vgwm.domain.value

import kotlinx.datetime.Instant

/**
 * AI-produced analysis envelope for an observation.
 */
data class AiAnalysis(
    val photoDescription: String?,
    val fiveW1H: FiveW1H?,
    val rootCauseAnalysis: RootCauseAnalysis?,
    val leanAnalysis: LeanAnalysis?,
    val generatedAt: Instant,
) {
    init {
        photoDescription?.let {
            require(it.isNotBlank()) { "AiAnalysis.photoDescription must not be blank when provided." }
        }

        require(
            !photoDescription.isNullOrBlank() ||
                fiveW1H != null ||
                rootCauseAnalysis != null ||
                leanAnalysis != null,
        ) {
            "AiAnalysis must contain at least one analysis component."
        }
    }
}
