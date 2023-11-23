package ru.gbzlat.database.models

import org.ktorm.database.Database
import org.ktorm.entity.Entity
import org.ktorm.entity.sequenceOf
import org.ktorm.schema.*
import ru.gbzlat.database

interface Priority : Entity<Priority> {
    companion object : Entity.Factory<Priority>()

    val id: Int
    val name: String
}

object Priorities : Table<Priority>("Priorities") {
    val id = int("id").primaryKey().bindTo { it.id }
    val name = varchar("name").bindTo { it.name }

    val Database.priorities get() = database.sequenceOf(Priorities)
}

/*@Serializable
data class Priority (
    val id: Int,
    val name: String
)

object Priorities: BaseTable<Priority>("Priorities") {
    val id = int("id").primaryKey()
    val name = varchar("name")

    override fun doCreateEntity(row: QueryRowSet, withReferences: Boolean)= Priority(
        id = row[id]!!,
        name = row[name]!!
    )
}*/

