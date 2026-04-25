package nl.freek.vgwm.config

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.v1.jdbc.Database

/**
 * Initializes the database connection for the application.
 *
 * This object only configures and connects the datasource. It does not create schemas,
 * run migrations, or execute SQL.
 */
object DatabaseConfig {

    /**
     * Creates a small HikariCP pool and connects Exposed to it.
     *
     * Call this explicitly during application startup after [AppConfig] is loaded.
     */
    fun initialize(appConfig: AppConfig): Database {
        val hikariConfig = HikariConfig().apply {
            jdbcUrl = appConfig.database.url
            username = appConfig.database.user
            password = appConfig.database.password
            driverClassName = "org.sqlite.JDBC"
            maximumPoolSize = 1
            minimumIdle = 1
            connectionTestQuery = "SELECT 1"
            poolName = "vgwm-sqlite-pool"
        }

        val dataSource = HikariDataSource(hikariConfig)
        return Database.connect(dataSource)
    }
}
