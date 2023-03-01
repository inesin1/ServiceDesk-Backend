package ru.gbzlat.database.models

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.ktorm.dsl.QueryRowSet
import org.ktorm.entity.Entity
import org.ktorm.entity.sequenceOf
import org.ktorm.schema.*
import ru.gbzlat.database.DatabaseManager
import ru.gbzlat.plugins.LocalDateTimeSerializer
import java.time.LocalDateTime

@Serializable
data class Status (
    val id: Int,
    val name: String
)

object Statuses: BaseTable<Status>("Statuses") {
    val id = int("id").primaryKey()
    val name = varchar("name")

    override fun doCreateEntity(row: QueryRowSet, withReferences: Boolean)= Status(
        id = row[id] ?: 0,
        name = row[name].orEmpty()
    )
}

val DatabaseManager.statuses get() = database.sequenceOf(Statuses)