package ru.gbzlat.db

import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.jdbc.selectAll
import ru.gbzlat.dto.Ref
import ru.gbzlat.dto.TicketCommentResponse
import ru.gbzlat.dto.TicketResponse
import ru.gbzlat.dto.UserResponse

fun RefTable.toRef(row: ResultRow) = Ref(row[id], row[name])

fun RefTable.allRefs() = selectAll().orderBy(id).map { toRef(it) }

fun RefTable.findRef(refId: Int) = selectAll().where { id eq refId }.singleOrNull()?.let { toRef(it) }

/**
 * Loads users by id along with their role, and their departments when asked for.
 * Tickets and comments embed users, so they resolve them through this in one go
 * instead of joining the users table once per reference.
 */
fun loadUsers(ids: Collection<Int>, withDepartments: Boolean = false): Map<Int, UserResponse> {
    if (ids.isEmpty()) return emptyMap()

    val departments = if (withDepartments) departmentsByUser(ids) else emptyMap()

    return Users.innerJoin(Roles)
        .selectAll()
        .where { Users.id inList ids }
        .associate { row ->
            row[Users.id] to UserResponse(
                id = row[Users.id],
                name = row[Users.name],
                login = row[Users.login],
                role = Ref(row[Roles.id], row[Roles.name]),
                phone = row[Users.phone],
                tgChatId = row[Users.tgChatId],
                departments = if (withDepartments) departments[row[Users.id]].orEmpty() else null,
            )
        }
}

fun departmentsByUser(userIds: Collection<Int>): Map<Int, List<Ref>> =
    UserDepartments.innerJoin(Departments)
        .selectAll()
        .where { UserDepartments.userId inList userIds }
        .groupBy({ it[UserDepartments.userId] }, { Ref(it[Departments.id], it[Departments.name]) })

fun toTicketResponses(rows: List<ResultRow>): List<TicketResponse> {
    val userIds = rows.flatMap { listOfNotNull(it[Tickets.creatorId], it[Tickets.executorId]) }.toSet()
    val users = loadUsers(userIds)

    return rows.map { row ->
        TicketResponse(
            id = row[Tickets.id],
            creatorId = row[Tickets.creatorId],
            creator = users.getValue(row[Tickets.creatorId]),
            executorId = row[Tickets.executorId],
            executor = row[Tickets.executorId]?.let { users[it] },
            details = row[Tickets.details],
            source = Ref(row[TicketSources.id], row[TicketSources.name]),
            category = Ref(row[TicketCategories.id], row[TicketCategories.name]),
            status = Ref(row[Statuses.id], row[Statuses.name]),
            createdAt = row[Tickets.createdAt].toString(),
            closedAt = row[Tickets.closedAt]?.toString(),
            timeLimit = row[Tickets.timeLimit].toString(),
        )
    }
}

/** Tickets joined to every reference table they embed, minus the creator/executor users. */
fun ticketsWithRefs() = Tickets
    .innerJoin(TicketSources, { sourceId }, { TicketSources.id })
    .innerJoin(TicketCategories, { Tickets.categoryId }, { TicketCategories.id })
    .innerJoin(Statuses, { Tickets.statusId }, { Statuses.id })

fun toCommentResponses(rows: List<ResultRow>): List<TicketCommentResponse> {
    val users = loadUsers(rows.map { it[TicketComments.creatorId] }.toSet())

    return rows.map { row ->
        TicketCommentResponse(
            id = row[TicketComments.id],
            ticketId = row[TicketComments.ticketId],
            creator = users.getValue(row[TicketComments.creatorId]),
            text = row[TicketComments.text],
            createdAt = row[TicketComments.createdAt].toString(),
        )
    }
}
