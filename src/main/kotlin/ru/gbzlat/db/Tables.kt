package ru.gbzlat.db

import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.javatime.datetime

/** Reference table: nothing but an id and a name. Five of them behave identically. */
abstract class RefTable(tableName: String) : Table(tableName) {
    val id = integer("id").autoIncrement()
    val name = varchar("name", 255)

    override val primaryKey = PrimaryKey(id)
}

object Roles : RefTable("roles")
object Departments : RefTable("departments")
object Statuses : RefTable("statuses")
object TicketCategories : RefTable("ticket_categories")
object TicketSources : RefTable("ticket_sources")

object Users : Table("users") {
    val id = integer("id").autoIncrement()
    val name = varchar("name", 255)
    val login = varchar("login", 64).uniqueIndex()
    val password = varchar("password", 255)
    val roleId = integer("role_id").references(Roles.id)
    val phone = varchar("phone", 32).nullable()
    val tgChatId = long("tg_chat_id").nullable()

    override val primaryKey = PrimaryKey(id)
}

object UserDepartments : Table("user_departments") {
    val userId = integer("user_id").references(Users.id)
    val departmentId = integer("department_id").references(Departments.id)

    override val primaryKey = PrimaryKey(userId, departmentId)
}

object Tickets : Table("tickets") {
    val id = integer("id").autoIncrement()
    val creatorId = integer("creator_id").references(Users.id)
    val executorId = integer("executor_id").references(Users.id).nullable()
    val details = text("details").nullable()
    val sourceId = integer("source_id").references(TicketSources.id)
    val categoryId = integer("category_id").references(TicketCategories.id)
    val statusId = integer("status_id").references(Statuses.id)
    val createdAt = datetime("created_at")
    val closedAt = datetime("closed_at").nullable()
    val timeLimit = datetime("time_limit")

    override val primaryKey = PrimaryKey(id)
}

object TicketComments : Table("ticket_comments") {
    val id = integer("id").autoIncrement()
    val ticketId = integer("ticket_id").references(Tickets.id)
    val creatorId = integer("creator_id").references(Users.id)
    val text = text("text")
    val createdAt = datetime("created_at")

    override val primaryKey = PrimaryKey(id)
}

val allTables = arrayOf(
    Roles, Departments, Statuses, TicketCategories, TicketSources,
    Users, UserDepartments, Tickets, TicketComments,
)
