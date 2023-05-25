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
data class ProblemCategory (
    val id: Int,
    val name: String
)

object ProblemCategories: BaseTable<ProblemCategory>("ProblemCategories") {
    val id = int("id").primaryKey()
    val name = varchar("name")

    override fun doCreateEntity(row: QueryRowSet, withReferences: Boolean)= ProblemCategory(
        id = row[id]!!,
        name = row[name]!!
    )
}

val DatabaseManager.problemCategories get() = database.sequenceOf(ProblemCategories)