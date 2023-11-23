package ru.gbzlat.database.models

import kotlinx.serialization.Serializable
import org.ktorm.database.Database
import org.ktorm.entity.Entity
import org.ktorm.entity.sequenceOf
import org.ktorm.schema.*
import ru.gbzlat.database
import ru.gbzlat.plugins.LocalDateTimeSerializer
import java.time.LocalDateTime

interface TicketComment : Entity<TicketComment> {
    companion object : Entity.Factory<TicketComment>()

    var id: Int
    var ticketId: Int
    var creator: User
    var text: String
    @Serializable(with = LocalDateTimeSerializer::class)
    var createdAt: LocalDateTime
}

object TicketComments : Table<TicketComment>("TicketComments") {
    val id = int("id").primaryKey().bindTo { it.id }
    val ticketId = int("ticket_id").bindTo { it.ticketId }
    val creatorId = int("creator_id").references(Users) { it.creator }
    val text = varchar("text").bindTo { it.text }
    val createdAt = datetime("created_at").bindTo { it.createdAt }

    val Database.ticketComments get() = database.sequenceOf(TicketComments)
}

/*@Serializable
data class Comment (
    val id: Int,
    val ticketId: Int,
    val userId: Int,
    val text: String,
    @Serializable(with = LocalDateTimeSerializer::class)
    val createDate: LocalDateTime
)

object Comments: BaseTable<Comment>("Comments") {
    val id = int("id").primaryKey()
    val ticketId = int("ticket_id")
    val userId = int("user_id")
    val text = varchar("text")
    val createDate = datetime("create_date")

    override fun doCreateEntity(row: QueryRowSet, withReferences: Boolean)= Comment(
        id = row[id]!!,
        ticketId = row[ticketId]!!,
        userId = row[userId]!!,
        text = row[text]!!,
        createDate = row[createDate]!!
    )
}*/

