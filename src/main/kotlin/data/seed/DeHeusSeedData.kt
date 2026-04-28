package nl.freek.vgwm.data.seed

import nl.freek.vgwm.data.table.CategoriesTable
import nl.freek.vgwm.data.table.CheckpointCategoriesTable
import nl.freek.vgwm.data.table.CheckpointsTable
import nl.freek.vgwm.data.table.CompanyInfoTable
import nl.freek.vgwm.data.table.LocationsTable
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

/**
 * Deployment reference seed data for the De Heus Veghel placeholder setup.
 *
 * This seed is intentionally data-only and keeps the application framework company-agnostic.
 */
object DeHeusSeedData {

    private const val COMPANY_INFO_ID = "company-default"
    private const val ROOT_LOCATION_ID = "loc-root-veghel-001"
    private val checkpointNameRotation = listOf(
        "Walkways and access routes",
        "Machine guarding",
        "Housekeeping and cleanliness",
        "Emergency exits and safety equipment",
        "Environmental and spill risks",
    )
    private val checkpointCategoryRotation = listOf(
        setOf("V"),
        setOf("V", "G"),
        setOf("W"),
        setOf("V", "W"),
        setOf("M"),
    )

    private val categories = listOf(
        CategorySeed(
            id = "cat-v-veiligheid-001",
            code = "V",
            displayNameEn = "Safety",
            displayNameNl = "Veiligheid",
            colorHex = "#D32F2F",
        ),
        CategorySeed(
            id = "cat-g-gezondheid-001",
            code = "G",
            displayNameEn = "Health",
            displayNameNl = "Gezondheid",
            colorHex = "#1976D2",
        ),
        CategorySeed(
            id = "cat-w-welzijn-000001",
            code = "W",
            displayNameEn = "Wellbeing",
            displayNameNl = "Welzijn",
            colorHex = "#7B1FA2",
        ),
        CategorySeed(
            id = "cat-m-milieu-0000001",
            code = "M",
            displayNameEn = "Environment",
            displayNameNl = "Milieu",
            colorHex = "#388E3C",
        ),
    )

    private val floorLocations = (1..8).map { floor ->
        LocationSeed(
            id = "loc-floor-$floor-veghel001",
            name = "Floor $floor",
            parentLocationId = ROOT_LOCATION_ID,
            orderIndex = floor,
            attentionNote = null,
        )
    }

    private val checkpoints = floorLocations.mapIndexed { index, floor ->
        val checkpointName = checkpointNameRotation[index % checkpointNameRotation.size]
        CheckpointSeed(
            id = "chk-floor-${index + 1}-base01",
            locationId = floor.id,
            name = checkpointName,
            attentionNote = "Placeholder checkpoint for seed deployment data",
            categoryCodes = checkpointCategoryRotation[index % checkpointCategoryRotation.size],
        )
    }

    /**
     * Inserts reference deployment seed data when rows are missing.
     *
     * The operation is idempotent and runs inside one transaction.
     */
    fun seedIfNeeded() {
        transaction {
            seedCompanyInfoIfMissing()
            seedCategoriesIfMissing()
            seedLocationsIfMissing()
            seedCheckpointsIfMissing()
            seedCheckpointCategoriesIfMissing()
        }
    }

    private fun seedCompanyInfoIfMissing() {
        if (CompanyInfoTable.selectAll().any()) {
            return
        }

        CompanyInfoTable.insert { row ->
            row[id] = COMPANY_INFO_ID
            row[companyName] = "De Heus"
            row[displayName] = "De Heus Veghel VGWM"
            row[primaryColorHex] = "#1F4E79"
            row[secondaryColorHex] = "#F2A900"
            row[defaultLanguage] = "nl"
        }
    }

    private fun seedCategoriesIfMissing() {
        categories.forEach { seed ->
            val existsByCode = CategoriesTable
                .selectAll()
                .where { CategoriesTable.code eq seed.code }
                .any()

            if (existsByCode) {
                return@forEach
            }

            CategoriesTable.insert { row ->
                row[id] = EntityID(seed.id, CategoriesTable)
                row[code] = seed.code
                row[displayNameEn] = seed.displayNameEn
                row[displayNameNl] = seed.displayNameNl
                row[colorHex] = seed.colorHex
            }
        }
    }

    private fun seedLocationsIfMissing() {
        insertLocationIfMissing(
            LocationSeed(
                id = ROOT_LOCATION_ID,
                name = "De Heus Veghel",
                parentLocationId = null,
                orderIndex = 0,
                attentionNote = null,
            )
        )

        floorLocations.forEach(::insertLocationIfMissing)
    }

    private fun insertLocationIfMissing(seed: LocationSeed) {
        val exists = LocationsTable
            .selectAll()
            .where { LocationsTable.id eq seed.id }
            .any()

        if (exists) {
            return
        }

        LocationsTable.insert { row ->
            row[id] = EntityID(seed.id, LocationsTable)
            row[name] = seed.name
            row[parentLocationId] = seed.parentLocationId?.let { EntityID(it, LocationsTable) }
            row[orderIndex] = seed.orderIndex
            row[attentionNote] = seed.attentionNote
        }
    }

    private fun seedCheckpointsIfMissing() {
        checkpoints.forEach { seed ->
            val exists = CheckpointsTable
                .selectAll()
                .where { CheckpointsTable.id eq seed.id }
                .any()

            if (exists) {
                return@forEach
            }

            CheckpointsTable.insert { row ->
                row[id] = EntityID(seed.id, CheckpointsTable)
                row[locationId] = EntityID(seed.locationId, LocationsTable)
                row[name] = seed.name
                row[attentionNote] = seed.attentionNote
            }
        }
    }

    /**
     * Uses deterministic checkpoint/category pairs and inserts missing links only.
     */
    private fun seedCheckpointCategoriesIfMissing() {
        val categoryIdByCode = CategoriesTable
            .selectAll()
            .associate { row -> row[CategoriesTable.code] to row[CategoriesTable.id] }

        checkpoints.forEach { checkpoint ->
            checkpoint.categoryCodes.forEach { categoryCode ->
                val categoryId = categoryIdByCode[categoryCode] ?: return@forEach
                insertCheckpointCategoryIfMissing(checkpoint.id, categoryId)
            }
        }
    }

    private fun insertCheckpointCategoryIfMissing(checkpointId: String, categoryId: EntityID<String>) {
        val checkpointEntityId = EntityID(checkpointId, CheckpointsTable)
        val exists = CheckpointCategoriesTable
            .selectAll()
            .where {
                (CheckpointCategoriesTable.checkpointId eq checkpointEntityId) and
                    (CheckpointCategoriesTable.categoryId eq categoryId)
            }
            .any()

        if (exists) {
            return
        }

        CheckpointCategoriesTable.insert { row ->
            row[CheckpointCategoriesTable.checkpointId] = checkpointEntityId
            row[CheckpointCategoriesTable.categoryId] = categoryId
        }
    }

    private data class CategorySeed(
        val id: String,
        val code: String,
        val displayNameEn: String,
        val displayNameNl: String,
        val colorHex: String?,
    )

    private data class LocationSeed(
        val id: String,
        val name: String,
        val parentLocationId: String?,
        val orderIndex: Int,
        val attentionNote: String?,
    )

    private data class CheckpointSeed(
        val id: String,
        val locationId: String,
        val name: String,
        val attentionNote: String?,
        val categoryCodes: Set<String>,
    )

}
