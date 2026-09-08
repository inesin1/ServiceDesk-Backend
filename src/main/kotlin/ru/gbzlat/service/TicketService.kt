package ru.gbzlat.service

import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.jdbc.*
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import ru.gbzlat.db.*
import ru.gbzlat.dto.Page
import ru.gbzlat.dto.TicketCommentResponse
import ru.gbzlat.dto.TicketDTO
import ru.gbzlat.dto.TicketResponse
import ru.gbzlat.error.notFound
import ru.gbzlat.security.Role
import java.time.LocalDateTime
import java.time.ZoneId

private val zone: ZoneId = ZoneId.of("GMT+5")

const val STATUS_NEW = 1
const val STATUS_CLOSED = 2
const val STATUS_IN_WORK = 3

data class TicketFilter(
    val statuses: List<Int>? = null,
    val categories: List<Int>? = null,
    val creators: List<Int>? = null,
    val executors: List<Int>? = null,
    val departments: List<Int>? = null,
)

object TicketService {

    fun list(userId: Int, role: Role, filter: TicketFilter, limit: Int, offset: Long): Page<TicketResponse> =
        transaction {
            val where = buildWhere(userId, role, filter)

            val rows = ticketsWithRefs()
                .selectAll()
                .where(where)
                .orderBy(Tickets.id, SortOrder.DESC)
                .limit(limit)
                .offset(offset)
                .toList()

            Page(toTicketResponses(rows), Tickets.selectAll().where(where).count())
        }

    fun byId(id: Int): TicketResponse = transaction {
        toTicketResponses(ticketsWithRefs().selectAll().where { Tickets.id eq id }.toList())
    }.singleOrNull() ?: notFound("Заявка №$id не найдена")

    /** Returns the creator's name for the notification that follows. */
    fun create(data: TicketDTO, creatorId: Int): String = transaction {
        Tickets.insert {
            it[Tickets.creatorId] = creatorId
            it[sourceId] = data.sourceId
            it[categoryId] = data.categoryId
            it[statusId] = STATUS_NEW
            it[details] = data.details
            it[createdAt] = LocalDateTime.now(zone)
            it[timeLimit] = LocalDateTime.now(zone).plusDays(1)
        }

        Users.select(Users.name).where { Users.id eq creatorId }.single()[Users.name]
    }

    fun assignExecutor(ticketId: Int, executorId: Int) = transaction {
        Tickets.update({ Tickets.id eq ticketId }) {
            it[Tickets.executorId] = executorId
            it[statusId] = STATUS_IN_WORK
        }
    }.also { if (it == 0) notFound("Заявка №$ticketId не найдена") }

    fun close(ticketId: Int) = transaction {
        Tickets.update({ Tickets.id eq ticketId }) {
            it[statusId] = STATUS_CLOSED
            it[closedAt] = LocalDateTime.now(zone)
        }
    }.also { if (it == 0) notFound("Заявка №$ticketId не найдена") }

    fun comments(ticketId: Int): List<TicketCommentResponse> = transaction {
        toCommentResponses(
            TicketComments.selectAll()
                .where { TicketComments.ticketId eq ticketId }
                .orderBy(TicketComments.id)
                .toList()
        )
    }

    fun addComment(ticketId: Int, authorId: Int, text: String) = transaction {
        TicketComments.insert {
            it[TicketComments.ticketId] = ticketId
            it[creatorId] = authorId
            it[TicketComments.text] = text
            it[createdAt] = LocalDateTime.now(zone)
        }
    }

    fun specialistChatIds(): List<Long> = transaction {
        Users.select(Users.tgChatId)
            .where { (Users.roleId neq Role.EMPLOYEE.id) and Users.tgChatId.isNotNull() }
            .mapNotNull { it[Users.tgChatId] }
    }

    private fun buildWhere(userId: Int, role: Role, filter: TicketFilter): Op<Boolean> {
        var where: Op<Boolean> = Op.TRUE
        if (role == Role.EMPLOYEE) where = where and (Tickets.creatorId eq userId)

        filter.statuses?.let { where = where and (Tickets.statusId inList it) }
        filter.categories?.let { where = where and (Tickets.categoryId inList it) }
        filter.creators?.let { where = where and (Tickets.creatorId inList it) }
        filter.executors?.let { where = where and (Tickets.executorId inList it) }
        filter.departments?.let { departmentIds ->
            val userIds = UserDepartments
                .select(UserDepartments.userId)
                .where { UserDepartments.departmentId inList departmentIds }
                .map { it[UserDepartments.userId] }
            where = where and (Tickets.creatorId inList userIds)
        }

        return where
    }
}
