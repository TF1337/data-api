package nl.freek.vgwm.data.mapper

import kotlinx.serialization.json.Json
import nl.freek.vgwm.domain.value.AiAnalysis

/**
 * Converts persisted JSON payloads to and from [AiAnalysis].
 */
object AiAnalysisJsonMapper {
    private val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
        encodeDefaults = true
    }

    fun fromJson(raw: String?): AiAnalysis? =
        raw?.takeIf { it.isNotBlank() }
            ?.let { json.decodeFromString<AiAnalysisSurrogate>(it).toDomain() }

    fun toJson(analysis: AiAnalysis?): String? =
        analysis?.let { json.encodeToString(AiAnalysisSurrogate.fromDomain(it)) }
}