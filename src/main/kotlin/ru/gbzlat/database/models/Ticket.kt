package ru.gbzlat.database.models

import org.ktorm.dsl.QueryRowSet
import org.ktorm.entity.Entity
import org.ktorm.entity.sequenceOf
import org.ktorm.schema.*
import ru.gbzlat.database.DatabaseManager
import java.time.LocalDateTime

data class Ticket (
    val id: Int,
    val userId: Int,
    val text: String,
    val createDate: LocalDateTime,
    val closeDate: LocalDateTime,
    val priorityId: Int,
    val statusId: Int,
)

object Tickets: BaseTable<Ticket>("Ticket") {
    val id = int("id").primaryKey()
    val userId = int("user_id")
    val text = varchar("text")
    val createDate = datetime("create_date")
    val closeDate = datetime("create_date")
    val priorityId = int("priority_id")
    val statusId = int("status_id")

    override fun doCreateEntity(row: QueryRowSet, withReferences: Boolean)= Ticket(
        id = row[id] ?: 0,
        userId = row[userId] ?: 0,
        text = row[text].orEmpty(),
        createDate = row[createDate]?: LocalDateTime.now(),
        closeDate = row[closeDate]?: LocalDateTime.now(),
        priorityId = row[priorityId] ?: 0,
        statusId = row[statusId] ?: 0,
    )
}

val DatabaseManager.tickets get() = database.sequenceOf(Tickets)