package ru.gbzlat.database.models

import org.ktorm.database.Database
import org.ktorm.entity.Entity
import org.ktorm.entity.sequenceOf
import org.ktorm.schema.*
import ru.gbzlat.database

interface TicketCategory : Entity<TicketCategory> {
    companion object : Entity.Factory<TicketCategory>()

    val id: Int
    var name: String
}

object TicketCategories : Table<TicketCategory>("TicketCategories") {
    val id = int("id").primaryKey().bindTo { it.id }
    val name = varchar("name").bindTo { it.name }

    val Database.ticketCategories get() = database.sequenceOf(TicketCategories)
}

/*@Serializable
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
}*/

