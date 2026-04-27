package nl.freek.vgwm.domain.value

/**
 * Captures a five-why chain and resulting measure proposal.
 */
data class RootCauseAnalysis(
    val why1: String,
    val why2: String,
    val why3: String,
    val why4: String,
    val why5: String,
    val rootCause: String,
    val recommendedMeasure: String,
) {
    init {
        require(why1.isNotBlank()) { "RootCauseAnalysis.why1 must not be blank." }
        require(why2.isNotBlank()) { "RootCauseAnalysis.why2 must not be blank." }
        require(why3.isNotBlank()) { "RootCauseAnalysis.why3 must not be blank." }
        require(why4.isNotBlank()) { "RootCauseAnalysis.why4 must not be blank." }
        require(why5.isNotBlank()) { "RootCauseAnalysis.why5 must not be blank." }
        require(rootCause.isNotBlank()) { "RootCauseAnalysis.rootCause must not be blank." }
        require(recommendedMeasure.isNotBlank()) { "RootCauseAnalysis.recommendedMeasure must not be blank." }
    }
}
