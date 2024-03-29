package ru.gbzlat.database.models

import org.ktorm.database.Database
import org.ktorm.entity.Entity
import org.ktorm.entity.sequenceOf
import org.ktorm.schema.*
import ru.gbzlat.database

interface Status : Entity<Status> {
    companion object : Entity.Factory<Status>()

    var id: Int
    var name: String
}

object Statuses : Table<Status>("Statuses") {
    val id = int("id").primaryKey().bindTo { it.id }
    val name = varchar("name").bindTo { it.name }

    val Database.statuses get() = database.sequenceOf(Statuses)
}

/*@Serializable
data class Status (
    val id: Int,
    val name: String
)

object Statuses: BaseTable<Status>("Statuses") {
    val id = int("id").primaryKey()
    val name = varchar("name")

    override fun doCreateEntity(row: QueryRowSet, withReferences: Boolean)= Status(
        id = row[id]!!,
        name = row[name]!!
    )
}*/

