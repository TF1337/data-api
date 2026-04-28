package nl.freek.vgwm.data.external

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.Serializable
import nl.freek.vgwm.domain.service.ChatbotClient
import nl.freek.vgwm.domain.service.GeneratedObservationAnalysis
import nl.freek.vgwm.domain.service.ObservationAiContext
import nl.freek.vgwm.domain.service.PhotoAnalysisResult
import nl.freek.vgwm.domain.value.FiveW1H
import nl.freek.vgwm.domain.value.LeanAnalysis
import nl.freek.vgwm.domain.value.PdcaCycle
import nl.freek.vgwm.domain.value.RootCauseAnalysis

/**
 * HTTP implementation of [ChatbotClient] for the separate chatbot API.
 *
 * Chatbot API contract endpoints:
 * - POST `/api/ai/photo-description`
 * - POST `/api/ai/observation-analysis`
 */
class HttpChatbotClient(
    baseUrl: String,
    private val httpClient: HttpClient,
) : ChatbotClient {
    private val normalizedBaseUrl: String

    init {
        require(baseUrl.isNotBlank()) { "Chatbot base URL must not be blank." }
        normalizedBaseUrl = baseUrl.trim().removeSuffix("/")
    }

    /**
     * Calls the chatbot API for image-only analysis.
     */
    override suspend fun analyzePhoto(imageBase64: String, context: ObservationAiContext): PhotoAnalysisResult {
        require(imageBase64.isNotBlank()) { "Image base64 must not be blank." }

        val response = httpClient.post("$normalizedBaseUrl$ANALYZE_PHOTO_PATH") {
            contentType(ContentType.Application.Json)
            setBody(AnalyzePhotoRequestDto(imageBase64 = imageBase64, context = ObservationAiContextDto.fromDomain(context)))
        }.body<AnalyzePhotoResponseDto>()

        return PhotoAnalysisResult(
            description = response.description,
            leanAnalysis = response.leanAnalysis?.toDomain(),
        )
    }

    /**
     * Calls the chatbot API for full observation analysis generation.
     */
    override suspend fun generateObservationAnalysis(
        context: ObservationAiContext,
        imageBase64: String?,
    ): GeneratedObservationAnalysis {
        val response = httpClient.post("$normalizedBaseUrl$GENERATE_OBSERVATION_ANALYSIS_PATH") {
            contentType(ContentType.Application.Json)
            setBody(
                GenerateObservationAnalysisRequestDto(
                    context = ObservationAiContextDto.fromDomain(context),
                    imageBase64 = imageBase64,
                )
            )
        }.body<GenerateObservationAnalysisResponseDto>()

        return GeneratedObservationAnalysis(
            fiveW1H = response.fiveW1H.toDomain(),
            rootCauseAnalysis = response.rootCauseAnalysis.toDomain(),
            leanAnalysis = response.leanAnalysis?.toDomain(),
        )
    }

    private companion object {
        const val ANALYZE_PHOTO_PATH = "/api/ai/photo-description"
        const val GENERATE_OBSERVATION_ANALYSIS_PATH = "/api/ai/observation-analysis"
    }
}

@Serializable
private data class AnalyzePhotoRequestDto(
    val imageBase64: String,
    val context: ObservationAiContextDto,
)

@Serializable
private data class AnalyzePhotoResponseDto(
    val description: String,
    val leanAnalysis: LeanAnalysisDto? = null,
)

@Serializable
private data class GenerateObservationAnalysisRequestDto(
    val context: ObservationAiContextDto,
    val imageBase64: String? = null,
)

@Serializable
private data class GenerateObservationAnalysisResponseDto(
    val fiveW1H: FiveW1HDto,
    val rootCauseAnalysis: RootCauseAnalysisDto,
    val leanAnalysis: LeanAnalysisDto? = null,
)

@Serializable
private data class ObservationAiContextDto(
    val checkpointLabel: String,
    val locationLabel: String,
    val observerLabel: String,
    val assigneeLabel: String? = null,
    val observationType: String,
    val description: String,
    val observedAt: String,
) {
    companion object {
        fun fromDomain(context: ObservationAiContext): ObservationAiContextDto = ObservationAiContextDto(
            checkpointLabel = context.checkpointLabel,
            locationLabel = context.locationLabel,
            observerLabel = context.observerLabel,
            assigneeLabel = context.assigneeLabel,
            observationType = context.observationType.name,
            description = context.description,
            observedAt = context.observedAt.toString(),
        )
    }
}

@Serializable
private data class FiveW1HDto(
    val who: String,
    val what: String,
    val where: String,
    val whenText: String,
    val why: String,
    val how: String,
) {
    fun toDomain(): FiveW1H = FiveW1H(
        who = who,
        what = what,
        where = where,
        whenText = whenText,
        why = why,
        how = how,
    )
}

@Serializable
private data class RootCauseAnalysisDto(
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
}

@Serializable
private data class PdcaCycleDto(
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
}

@Serializable
private data class LeanAnalysisDto(
    val wasteType: String,
    val sixSCategory: String,
    val threeM: String,
    val processFocus: String,
    val gembaObservation: String,
    val pdca: PdcaCycleDto,
) {
    fun toDomain(): LeanAnalysis = LeanAnalysis(
        wasteType = wasteType,
        sixSCategory = sixSCategory,
        threeM = threeM,
        processFocus = processFocus,
        gembaObservation = gembaObservation,
        pdca = pdca.toDomain(),
    )
}