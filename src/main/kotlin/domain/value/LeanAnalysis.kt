package nl.freek.vgwm.domain.value

/**
 * Lean and operational-excellence interpretation for an observation.
 */
data class LeanAnalysis(
    val wasteType: String,
    val sixSCategory: String,
    val threeM: String,
    val processFocus: String,
    val gembaObservation: String,
    val pdca: PdcaCycle,
) {
    init {
        require(wasteType.isNotBlank()) { "LeanAnalysis.wasteType must not be blank." }
        require(sixSCategory.isNotBlank()) { "LeanAnalysis.sixSCategory must not be blank." }
        require(threeM.isNotBlank()) { "LeanAnalysis.threeM must not be blank." }
        require(processFocus.isNotBlank()) { "LeanAnalysis.processFocus must not be blank." }
        require(gembaObservation.isNotBlank()) { "LeanAnalysis.gembaObservation must not be blank." }
    }
}
