package ru.gbzlat.database.models

import kotlinx.serialization.Serializable
import org.ktorm.dsl.QueryRowSet
import org.ktorm.entity.sequenceOf
import org.ktorm.entity.toList
import org.ktorm.schema.*
import ru.gbzlat.database.DatabaseManager
import ru.gbzlat.database
import ru.gbzlat.plugins.LocalDateTimeSerializer
import java.time.LocalDateTime

@Serializable
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
}

val DatabaseManager.comments get() = database.sequenceOf(Comments)