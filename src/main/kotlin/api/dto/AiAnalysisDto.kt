package nl.freek.vgwm.api.dto

import kotlinx.serialization.Serializable

@Serializable
data class FiveW1HDto(
    val who: String,
    val what: String,
    val where: String,
    val whenText: String,
    val why: String,
    val how: String,
)

@Serializable
data class RootCauseAnalysisDto(
    val why1: String,
    val why2: String,
    val why3: String,
    val why4: String,
    val why5: String,
    val rootCause: String,
    val recommendedMeasure: String,
)

@Serializable
data class PdcaCycleDto(
    val plan: String,
    val doStep: String,
    val check: String,
    val act: String,
)

@Serializable
data class LeanAnalysisDto(
    val wasteType: String,
    val sixSCategory: String,
    val threeM: String,
    val processFocus: String,
    val gembaObservation: String,
    val pdca: PdcaCycleDto,
)

@Serializable
data class AiAnalysisDto(
    val photoDescription: String?,
    val fiveW1H: FiveW1HDto?,
    val rootCauseAnalysis: RootCauseAnalysisDto?,
    val leanAnalysis: LeanAnalysisDto?,
    val generatedAt: String,
)