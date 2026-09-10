package ru.gbzlat

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import ru.gbzlat.db.allTables
import kotlin.test.Test
import kotlin.test.assertEquals

class SchemaTest {

    /** The Flyway migrations are written by hand, so they can drift from the Exposed tables. */
    @Test
    fun `миграции описывают ту же схему, что и таблицы Exposed`() {
        val dataSource = HikariDataSource(HikariConfig().apply {
            jdbcUrl = postgres.jdbcUrl
            username = postgres.username
            password = postgres.password
            maximumPoolSize = 3
        })

        try {
            Flyway.configure().dataSource(dataSource).load().migrate()
            val db = Database.connect(dataSource)
            val missing = transaction(db) { SchemaUtils.statementsRequiredToActualizeScheme(*allTables) }

            assertEquals(emptyList(), missing)
        } finally {
            dataSource.close()
        }
    }
}
