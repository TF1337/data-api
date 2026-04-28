package nl.freek.vgwm.data.entity

import nl.freek.vgwm.data.table.ObservationsTable
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.Entity
import org.jetbrains.exposed.v1.dao.EntityClass

/**
 * Exposed DAO entity for rows stored in [ObservationsTable].
 */
class ObservationEntity(id: EntityID<String>) : Entity<String>(id) {
    companion object : EntityClass<String, ObservationEntity>(ObservationsTable)

    var createdAt by ObservationsTable.createdAt
    var updatedAt by ObservationsTable.updatedAt
    var tourId by ObservationsTable.tourId
    var checkpointId by ObservationsTable.checkpointId
    var observerId by ObservationsTable.observerId
    var assigneeId by ObservationsTable.assigneeId
    var categoryId by ObservationsTable.categoryId
    var observationType by ObservationsTable.observationType
    var description by ObservationsTable.description
    var priorityCode by ObservationsTable.priorityCode
    var status by ObservationsTable.status
    var photoPath by ObservationsTable.photoPath
    var externalRef by ObservationsTable.externalRef
    var aiAnalysisJson by ObservationsTable.aiAnalysisJson
    var aiGeneratedAt by ObservationsTable.aiGeneratedAt
}
