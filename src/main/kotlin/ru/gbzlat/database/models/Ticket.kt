package ru.gbzlat.database.models

import org.ktorm.database.Database
import org.ktorm.entity.Entity
import org.ktorm.entity.sequenceOf
import org.ktorm.schema.*
import ru.gbzlat.database
import java.time.LocalDateTime

interface Ticket : Entity<Ticket> {
    companion object : Entity.Factory<Ticket>()

    val id: Int
    var creator: User
    var executor: User?
    var details: String?
    var source: TicketSource
    var category: TicketCategory
    //@Serializable(with = LocalDateTimeSerializer::class)
    var createdAt: LocalDateTime
    //@Serializable(with = LocalDateTimeSerializer::class)
    var closedAt: LocalDateTime?
    //@Serializable(with = LocalDateTimeSerializer::class)
    var timeLimit: LocalDateTime
    var status: Status
}

object Tickets : Table<Ticket>("Tickets") {
    val id = int("id").primaryKey().bindTo { it.id }
    val creatorId = int("creator_id").references(Users) { it.creator }
    val executorId = int("executor_id").references(Users) { it.executor }
    val details = varchar("details").bindTo { it.details }
    val sourceId = int("source_id").references(TicketSources) { it.source }
    val categoryId = int("category_id").references(TicketCategories) { it.category }
    val createdAt = datetime("created_at").bindTo { it.createdAt }
    val closedAt = datetime("closed_at").bindTo { it.closedAt }
    val timeLimit = datetime("time_limit").bindTo { it.timeLimit }
    val statusId = int("status_id").references(Statuses) { it.status }

    val Database.tickets get() = database.sequenceOf(Tickets)
}

/*@Serializable
data class Ticket (
    val id: Int,
    val creatorId: Int,
    val executorId: Int?,
    val details: String?,
    val categoryId: Int,
    @Serializable(with = LocalDateTimeSerializer::class)
    val createDate: LocalDateTime,
    @Serializable(with = LocalDateTimeSerializer::class)
    val closeDate: LocalDateTime?,
    @Serializable(with = LocalDateTimeSerializer::class)
    val timeLimit: LocalDateTime,
    val statusId: Int,
)*/

/*object Tickets: BaseTable<Ticket>("Tickets") {
    val id = int("id").primaryKey()
    val creatorId = int("creator_id")
    val executorId = int("executor_id")
    val details = varchar("details")
    val categoryId = int("category_id")
    val createDate = datetime("create_date")
    val closeDate = datetime("close_date")
    val timeLimit = datetime("time_limit")
    val statusId = int("status_id")

    override fun doCreateEntity(row: QueryRowSet, withReferences: Boolean)= Ticket(
        id = row[id]!!,
        creatorId = row[creatorId]!!,
        executorId = row[executorId],
        details = row[details].orEmpty(),
        categoryId = row[categoryId]!!,
        createDate = row[createDate]?: LocalDateTime.now(),
        closeDate = row[closeDate],
        timeLimit = row[timeLimit]!!,
        statusId = row[statusId]!!
    )
}*/