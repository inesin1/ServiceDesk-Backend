package ru.gbzlat.database.models

import kotlinx.serialization.Serializable
import org.ktorm.dsl.QueryRowSet
import org.ktorm.entity.sequenceOf
import org.ktorm.schema.*
import ru.gbzlat.database.DatabaseManager
import ru.gbzlat.plugins.LocalDateTimeSerializer
import java.time.LocalDateTime

@Serializable
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
)

object Tickets: BaseTable<Ticket>("Tickets") {
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
}

val DatabaseManager.tickets get() = database.sequenceOf(Tickets)