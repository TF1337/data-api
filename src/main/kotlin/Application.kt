package nl.freek.vgwm

import io.ktor.server.application.Application
import io.ktor.server.application.install
import nl.freek.vgwm.config.AppConfig
import nl.freek.vgwm.config.DatabaseConfig
import nl.freek.vgwm.data.seed.DeHeusSeedData
import nl.freek.vgwm.data.table.CategoriesTable
import nl.freek.vgwm.data.table.CheckpointCategoriesTable
import nl.freek.vgwm.data.table.CheckpointsTable
import nl.freek.vgwm.data.table.CompanyInfoTable
import nl.freek.vgwm.data.table.LocationsTable
import nl.freek.vgwm.data.table.ObservationsTable
import nl.freek.vgwm.data.table.TourCheckpointProgressTable
import nl.freek.vgwm.data.table.TourParticipantsTable
import nl.freek.vgwm.data.table.ToursTable
import nl.freek.vgwm.data.table.UsersTable
import nl.freek.vgwm.di.appModule
import nl.freek.vgwm.plugins.configureAuth
import nl.freek.vgwm.plugins.configureCallLogging
import nl.freek.vgwm.plugins.configureHTTP
import nl.freek.vgwm.plugins.configureRouting
import nl.freek.vgwm.plugins.configureSerialization
import nl.freek.vgwm.plugins.configureStatusPages
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.koin.ktor.plugin.Koin

fun Application.module() {
    val appConfig = AppConfig.fromEnvironment()

    DatabaseConfig.initialize(appConfig)
    transaction {
        SchemaUtils.createMissingTablesAndColumns(
            UsersTable,
            LocationsTable,
            CategoriesTable,
            CheckpointsTable,
            CheckpointCategoriesTable,
            ToursTable,
            TourParticipantsTable,
            TourCheckpointProgressTable,
            ObservationsTable,
            CompanyInfoTable,
        )
    }
    DeHeusSeedData.seedIfNeeded()

    install(Koin) {
        modules(appModule(appConfig))
    }

    configureCallLogging()
    configureStatusPages()
    configureHTTP()
    configureSerialization()
    configureAuth(appConfig)
    configureRouting()
}
