package ru.gbzlat.database

import io.ktor.server.application.*
import org.ktorm.database.Database
import org.ktorm.dsl.from
import org.ktorm.dsl.map
import org.ktorm.dsl.select
import org.ktorm.entity.Entity
import org.ktorm.entity.sequenceOf
import org.ktorm.entity.toList
import org.ktorm.schema.Table

import ru.gbzlat.database.models.users

class DatabaseManager {
    private val hostname = "127.0.0.1"
    private val databaseName = "service_desk"
    private val username = "artem"
    private val password = "bujhmytcby123"

    val database: Database

    init {
        val jdbcUrl = "jdbc:mysql://$hostname:3306/$databaseName?user=$username&password=$password&allowPublicKeyRetrieval=true&useSSL=false"
        database = Database.connect(jdbcUrl)
    }
}