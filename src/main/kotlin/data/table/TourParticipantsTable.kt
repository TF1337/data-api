package nl.freek.vgwm.data.table

import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.Table

/**
 * Junction table linking tours with participating users.
 */
object TourParticipantsTable : Table("tour_participants") {
    val tourId = reference(
        name = "tour_id",
        foreign = ToursTable,
        onDelete = ReferenceOption.CASCADE,
    )
    val userId = reference(
        name = "user_id",
        foreign = UsersTable,
        onDelete = ReferenceOption.RESTRICT,
    )

    override val primaryKey = PrimaryKey(tourId, userId)
}
