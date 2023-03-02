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
data class Division (
    val id: Int,
    val name: String,
    val programmerId: Int
)

object Divisions: BaseTable<Division>("Divisions") {
    val id = int("id").primaryKey()
    val name = varchar("name")
    val programmerId = int("programmer_id")

    override fun doCreateEntity(row: QueryRowSet, withReferences: Boolean)= Division(
        id = row[id] ?: 0,
        name = row[name].orEmpty(),
        programmerId = row[programmerId] ?: 1
    )
}

val DatabaseManager.divisions get() = database.sequenceOf(Divisions)