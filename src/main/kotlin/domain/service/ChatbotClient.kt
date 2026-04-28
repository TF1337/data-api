package nl.freek.vgwm.domain.service

import kotlinx.datetime.Instant
import nl.freek.vgwm.domain.value.FiveW1H
import nl.freek.vgwm.domain.value.LeanAnalysis
import nl.freek.vgwm.domain.value.ObservationType
import nl.freek.vgwm.domain.value.RootCauseAnalysis

/**
 * Strategy contract for requesting AI analysis from the internal chatbot service.
 */
interface ChatbotClient {
    /**
     * Analyzes a photo and returns a textual description with optional lean analysis.
     */
    suspend fun analyzePhoto(imageBase64: String, context: ObservationAiContext): PhotoAnalysisResult

    /**
     * Generates a structured observation analysis for the provided context.
     */
    suspend fun generateObservationAnalysis(
        context: ObservationAiContext,
        imageBase64: String?,
    ): GeneratedObservationAnalysis
}

/**
 * Immutable context payload for AI analysis requests.
 */
data class ObservationAiContext(
    val checkpointLabel: String,
    val locationLabel: String,
    val observerLabel: String,
    val assigneeLabel: String?,
    val observationType: ObservationType,
    val description: String,
    val observedAt: Instant,
) {
    init {
        require(checkpointLabel.isNotBlank()) { "Observation AI context checkpointLabel must not be blank." }
        require(locationLabel.isNotBlank()) { "Observation AI context locationLabel must not be blank." }
        require(observerLabel.isNotBlank()) { "Observation AI context observerLabel must not be blank." }
        assigneeLabel?.let {
            require(it.isNotBlank()) { "Observation AI context assigneeLabel must not be blank when provided." }
        }
        require(description.isNotBlank()) { "Observation AI context description must not be blank." }
    }

    /**
     * Returns a new context with an updated description.
     */
    fun withDescription(description: String): ObservationAiContext = copy(description = description)
}

/**
 * Result of photo-only AI analysis.
 */
data class PhotoAnalysisResult(
    val description: String,
    val leanAnalysis: LeanAnalysis?,
) {
    init {
        require(description.isNotBlank()) { "Photo analysis description must not be blank." }
    }
}

/**
 * Result of full AI observation analysis generation.
 */
data class GeneratedObservationAnalysis(
    val fiveW1H: FiveW1H,
    val rootCauseAnalysis: RootCauseAnalysis,
    val leanAnalysis: LeanAnalysis?,
)
