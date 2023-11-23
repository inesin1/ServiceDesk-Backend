package ru.gbzlat.database.models

import org.ktorm.database.Database
import org.ktorm.entity.Entity
import org.ktorm.entity.sequenceOf
import org.ktorm.schema.*
import ru.gbzlat.database

interface Department : Entity<Department> {
    companion object : Entity.Factory<Department>()

    val id: Int
    var name: String
}

object Departments : Table<Department>("Departments") {
    val id = int("id").primaryKey().bindTo { it.id }
    val name = varchar("name").bindTo { it.name }

    val Database.departments get() = database.sequenceOf(Departments)
}

/*@Serializable
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

        division = database.divisions.find { it.id eq row[divisionId]!!.toInt() }!!
    )
}*/

