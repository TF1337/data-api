package nl.freek.vgwm.api.dto.mapper

import kotlinx.datetime.Instant
import nl.freek.vgwm.api.dto.AiAnalysisDto
import nl.freek.vgwm.api.dto.FiveW1HDto
import nl.freek.vgwm.api.dto.LeanAnalysisDto
import nl.freek.vgwm.api.dto.PdcaCycleDto
import nl.freek.vgwm.api.dto.RootCauseAnalysisDto
import nl.freek.vgwm.domain.value.AiAnalysis
import nl.freek.vgwm.domain.value.FiveW1H
import nl.freek.vgwm.domain.value.LeanAnalysis
import nl.freek.vgwm.domain.value.PdcaCycle
import nl.freek.vgwm.domain.value.RootCauseAnalysis

fun FiveW1H.toDto(): FiveW1HDto = FiveW1HDto(
    who = who,
    what = what,
    where = where,
    whenText = whenText,
    why = why,
    how = how,
)

fun RootCauseAnalysis.toDto(): RootCauseAnalysisDto = RootCauseAnalysisDto(
    why1 = why1,
    why2 = why2,
    why3 = why3,
    why4 = why4,
    why5 = why5,
    rootCause = rootCause,
    recommendedMeasure = recommendedMeasure,
)

fun PdcaCycle.toDto(): PdcaCycleDto = PdcaCycleDto(
    plan = plan,
    doStep = doStep,
    check = check,
    act = act,
)

fun LeanAnalysis.toDto(): LeanAnalysisDto = LeanAnalysisDto(
    wasteType = wasteType,
    sixSCategory = sixSCategory,
    threeM = threeM,
    processFocus = processFocus,
    gembaObservation = gembaObservation,
    pdca = pdca.toDto(),
)

fun AiAnalysis.toDto(): AiAnalysisDto = AiAnalysisDto(
    photoDescription = photoDescription,
    fiveW1H = fiveW1H?.toDto(),
    rootCauseAnalysis = rootCauseAnalysis?.toDto(),
    leanAnalysis = leanAnalysis?.toDto(),
    generatedAt = generatedAt.toString(),
)

fun FiveW1HDto.toDomain(): FiveW1H = FiveW1H(
    who = who,
    what = what,
    where = where,
    whenText = whenText,
    why = why,
    how = how,
)

fun RootCauseAnalysisDto.toDomain(): RootCauseAnalysis = RootCauseAnalysis(
    why1 = why1,
    why2 = why2,
    why3 = why3,
    why4 = why4,
    why5 = why5,
    rootCause = rootCause,
    recommendedMeasure = recommendedMeasure,
)

fun PdcaCycleDto.toDomain(): PdcaCycle = PdcaCycle(
    plan = plan,
    doStep = doStep,
    check = check,
    act = act,
)

fun LeanAnalysisDto.toDomain(): LeanAnalysis = LeanAnalysis(
    wasteType = wasteType,
    sixSCategory = sixSCategory,
    threeM = threeM,
    processFocus = processFocus,
    gembaObservation = gembaObservation,
    pdca = pdca.toDomain(),
)

fun AiAnalysisDto.toDomain(): AiAnalysis = AiAnalysis(
    photoDescription = photoDescription,
    fiveW1H = fiveW1H?.toDomain(),
    rootCauseAnalysis = rootCauseAnalysis?.toDomain(),
    leanAnalysis = leanAnalysis?.toDomain(),
    generatedAt = parseGeneratedAt(generatedAt),
)

private fun parseGeneratedAt(raw: String): Instant = try {
    Instant.parse(raw)
} catch (_: IllegalArgumentException) {
    throw IllegalArgumentException("Invalid 'aiAnalysis.generatedAt' value: $raw")
}