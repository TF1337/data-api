package nl.freek.vgwm.domain.service

import java.util.Base64
import kotlinx.datetime.Clock
import nl.freek.vgwm.domain.entity.Observation
import nl.freek.vgwm.domain.id.CategoryId
import nl.freek.vgwm.domain.id.CheckpointId
import nl.freek.vgwm.domain.id.TourId
import nl.freek.vgwm.domain.id.UserId
import nl.freek.vgwm.domain.repository.ObservationRepository
import nl.freek.vgwm.domain.repository.TourRepository
import nl.freek.vgwm.domain.value.AiAnalysis
import nl.freek.vgwm.domain.value.ObservationType
import nl.freek.vgwm.domain.value.Priority
import nl.freek.vgwm.domain.value.TourStatus

/**
 * Request model for observation creation orchestration.
 *
 * TODO(requirement): Implement ObservationCreationService orchestration in Section 16 item 14B.
 */
data class NewObservationRequest(
    val checkpointId: CheckpointId,
    val observerId: UserId,
    val assigneeId: UserId? = null,
    val categoryId: CategoryId,
    val observationType: ObservationType,
    val description: String,
    val priority: Priority,
    val tourId: TourId? = null,
    val photoBytes: ByteArray? = null,
    val aiContext: ObservationAiContext? = null,
    val requestAiAnalysis: Boolean = false,
    val aiAnalysis: AiAnalysis? = null,
) {
    init {
        require(description.isNotBlank()) { "New observation description must not be blank." }
    }
}

/**
 * Application service that orchestrates observation creation, optional photo storage,
 * and optional AI enrichment.
 */
class ObservationCreationService(
    private val observations: ObservationRepository,
    private val tours: TourRepository,
    private val chatbot: ChatbotClient,
    private val photoStorage: PhotoStorage,
) {
    class DraftAiAnalysisException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)
    /**
     * Creates a new observation and enriches it with AI analysis when photo bytes are available.
     *
     * Photo storage failures are propagated to avoid persisting observations with missing photo references.
     * AI failures are fail-soft: observation creation continues without AI analysis.
     */
    suspend fun createWithAiAnalysis(request: NewObservationRequest): Observation {
        ensureTourSupportsObservationCreate(request.tourId)

        val photoPath = request.photoBytes?.let { photoStorage.save(it) }

        var observation = Observation.create(
            checkpointId = request.checkpointId,
            observerId = request.observerId,
            categoryId = request.categoryId,
            observationType = request.observationType,
            description = request.description,
            priority = request.priority,
            tourId = request.tourId,
            photoPath = photoPath,
        )

        if (request.assigneeId != null) {
            observation = observation.assignTo(request.assigneeId)
        }

        if (!request.requestAiAnalysis && request.aiAnalysis != null) {
            observation = observation.withAiAnalysis(request.aiAnalysis)
        }

        if (request.requestAiAnalysis) {
            require(request.photoBytes != null) { "photoBytes is required when requestAiAnalysis is true." }
            require(request.aiContext != null) { "aiContext is required when requestAiAnalysis is true." }

            try {
                val imageBase64 = Base64.getEncoder().encodeToString(request.photoBytes)
                val photoAnalysis = chatbot.analyzePhoto(
                    imageBase64 = imageBase64,
                    context = request.aiContext,
                )
                val generatedAnalysis = chatbot.generateObservationAnalysis(
                    context = request.aiContext.withDescription(
                        request.description + "\n\nPhoto analysis: " + photoAnalysis.description,
                    ),
                    imageBase64 = imageBase64,
                )

                observation = observation.withAiAnalysis(
                    AiAnalysis(
                        photoDescription = photoAnalysis.description,
                        fiveW1H = generatedAnalysis.fiveW1H,
                        rootCauseAnalysis = generatedAnalysis.rootCauseAnalysis,
                        leanAnalysis = generatedAnalysis.leanAnalysis ?: photoAnalysis.leanAnalysis,
                        generatedAt = Clock.System.now(),
                    )
                )
            } catch (_: Exception) {
                // AI enrichment is optional. Keep observation creation successful without aiAnalysis.
            }
        }

        return observations.save(observation)
    }

    private suspend fun ensureTourSupportsObservationCreate(tourId: TourId?) {
        if (tourId == null) {
            return
        }

        val linkedTour = tours.findById(tourId) ?: throw IllegalArgumentException("TOUR_NOT_FOUND")
        if (linkedTour.status != TourStatus.IN_PROGRESS) {
            throw IllegalArgumentException("TOUR_NOT_ACTIVE_FOR_OBSERVATION_CREATE")
        }
    }

    /**
     * Generates draft AI analysis from photo bytes and AI context without persisting anything.
     */
    suspend fun generateDraftAiAnalysis(photoBytes: ByteArray, aiContext: ObservationAiContext): AiAnalysis {
        return try {
            val imageBase64 = Base64.getEncoder().encodeToString(photoBytes)
            val photoAnalysis = chatbot.analyzePhoto(
                imageBase64 = imageBase64,
                context = aiContext,
            )
            val generatedAnalysis = chatbot.generateObservationAnalysis(
                context = aiContext.withDescription(
                    aiContext.description + "\n\nPhoto analysis: " + photoAnalysis.description,
                ),
                imageBase64 = imageBase64,
            )

            AiAnalysis(
                photoDescription = photoAnalysis.description,
                fiveW1H = generatedAnalysis.fiveW1H,
                rootCauseAnalysis = generatedAnalysis.rootCauseAnalysis,
                leanAnalysis = generatedAnalysis.leanAnalysis ?: photoAnalysis.leanAnalysis,
                generatedAt = Clock.System.now(),
            )
        } catch (exception: Exception) {
            throw DraftAiAnalysisException("Draft AI analysis generation failed.", exception)
        }
    }
}
