package ru.gbzlat.database.models

import org.ktorm.database.Database
import org.ktorm.entity.Entity
import org.ktorm.entity.sequenceOf
import org.ktorm.schema.Table
import org.ktorm.schema.int
import org.ktorm.schema.varchar
import ru.gbzlat.database

interface TicketSource : Entity<TicketSource> {
    companion object : Entity.Factory<TicketSource>()

    val id: Int
    var name: String
}

object TicketSources : Table<TicketSource>("TicketSources") {
    val id = int("id").primaryKey().bindTo { it.id }
    val name = varchar("name").bindTo { it.name }

    val Database.ticketSources get() = database.sequenceOf(TicketSources)
}