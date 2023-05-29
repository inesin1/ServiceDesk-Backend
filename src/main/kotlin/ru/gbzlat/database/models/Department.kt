package ru.gbzlat.database.models

import kotlinx.serialization.Serializable
import org.ktorm.dsl.QueryRowSet
import org.ktorm.entity.sequenceOf
import org.ktorm.entity.toList
import org.ktorm.schema.*
import ru.gbzlat.database.DatabaseManager
import ru.gbzlat.database

@Serializable
data class Department (
    val id: Int,
    val name: String,
    val divisionId: Int,

    val division: Division
)

object Departments: BaseTable<Department>("Departments") {
    val id = int("id").primaryKey()
    val name = varchar("name")
    val divisionId = int("division_id")

    override fun doCreateEntity(row: QueryRowSet, withReferences: Boolean)= Department(
        id = row[id]!!,
        name = row[name]!!,
        divisionId = row[divisionId]!!,

        division = database.divisions.toList().single { it.id == row[divisionId] }
    )
}

val DatabaseManager.departments get() = database.sequenceOf(Departments)