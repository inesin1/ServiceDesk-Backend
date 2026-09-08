package ru.gbzlat.service

import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.jdbc.*
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import ru.gbzlat.db.*
import ru.gbzlat.dto.Page
import ru.gbzlat.dto.Ref
import ru.gbzlat.dto.UserDTO
import ru.gbzlat.dto.UserResponse
import ru.gbzlat.error.notFound
import ru.gbzlat.security.Role
import ru.gbzlat.security.hashIfPlaintext
import ru.gbzlat.security.hashPassword
import java.io.File

object UserService {

    fun list(limit: Int, offset: Long, withDepartments: Boolean): Page<UserResponse> = transaction {
        val ids = Users.select(Users.id).orderBy(Users.id).limit(limit).offset(offset)
            .map { it[Users.id] }

        Page(loadUsers(ids, withDepartments).values.sortedBy { it.id }, Users.selectAll().count())
    }

    fun specialists(withDepartments: Boolean): List<UserResponse> = transaction {
        val ids = Users.select(Users.id).where { Users.roleId neq Role.EMPLOYEE.id }
            .map { it[Users.id] }
        loadUsers(ids, withDepartments).values.sortedBy { it.id }
    }

    fun byId(id: Int, withDepartments: Boolean): UserResponse =
        transaction { loadUsers(listOf(id), withDepartments)[id] } ?: notFound("Пользователь №$id не найден")

    fun isLoginTaken(login: String): Boolean =
        transaction { Users.selectAll().where { Users.login eq login }.any() }

    fun create(data: UserDTO): UserResponse = transaction {
        val id = Users.insert {
            it[name] = data.name
            it[login] = data.login
            it[password] = hashPassword(data.password)
            it[roleId] = data.roleId
            it[phone] = data.phone
            it[tgChatId] = data.tgChatId
        } get Users.id

        setDepartments(id, data.departmentIds)
        loadUsers(listOf(id), withDepartments = true).getValue(id)
    }

    /** Only an admin may change a role or departments; the account holder edits the rest. */
    fun update(id: Int, data: UserDTO, asAdmin: Boolean): UserResponse = transaction {
        val rows = Users.update({ Users.id eq id }) {
            it[name] = data.name
            it[login] = data.login
            it[password] = hashPassword(data.password)
            it[phone] = data.phone
            it[tgChatId] = data.tgChatId
            if (asAdmin) it[roleId] = data.roleId
        }
        if (rows == 0) notFound("Пользователь №$id не найден")
        if (asAdmin) setDepartments(id, data.departmentIds)

        loadUsers(listOf(id), withDepartments = true).getValue(id)
    }

    fun delete(id: Int) = transaction {
        UserDepartments.deleteWhere { userId eq id }
        Users.deleteWhere { Users.id eq id }
    }.also { if (it == 0) notFound("Пользователь №$id не найден") }

    fun departmentsOf(userId: Int): List<Ref> =
        transaction { departmentsByUser(listOf(userId))[userId].orEmpty() }

    fun addDepartments(userId: Int, departmentIds: List<Int>) = transaction {
        UserDepartments.batchInsert(departmentIds, ignore = true) { departmentId ->
            this[UserDepartments.userId] = userId
            this[UserDepartments.departmentId] = departmentId
        }
    }

    fun removeDepartments(userId: Int, departmentIds: List<Int>) = transaction {
        UserDepartments.deleteWhere {
            (UserDepartments.userId eq userId) and (departmentId inList departmentIds)
        }
    }

    fun importFrom(file: File, rewrite: Boolean): Int = transaction {
        if (rewrite) {
            backup()
            UserDepartments.deleteAll()
            Users.deleteAll()
        }

        file.readLines().count { importLine(it) }
    }

    private fun backup() {
        File("backup").mkdirs()
        File("backup/users_${System.currentTimeMillis()}.tsv")
            .writeText(Users.selectAll().joinToString("\n") { row ->
                listOf(
                    row[Users.name], row[Users.login], row[Users.password],
                    row[Users.roleId], "", row[Users.phone].orEmpty(), row[Users.tgChatId] ?: ""
                ).joinToString("\t")
            })
    }

    /** Tab-separated: name, login, password, roleId, unused, phone, tgChatId. */
    private fun importLine(line: String): Boolean {
        val cells = line.split('\t')
        if (cells.size < 7) return false

        val login = cells[1]
        val role = cells[3].toIntOrNull() ?: return false
        val chatId = cells[6].toLongOrNull()

        if (Users.selectAll().where { Users.login eq login }.empty()) {
            Users.insert {
                it[name] = cells[0]
                it[Users.login] = login
                it[password] = hashIfPlaintext(cells[2])
                it[roleId] = role
                it[phone] = cells[5]
                it[tgChatId] = chatId
            }
        } else {
            Users.update({ Users.login eq login }) {
                it[name] = cells[0]
                it[password] = hashIfPlaintext(cells[2])
                it[roleId] = role
                it[phone] = cells[5]
                it[tgChatId] = chatId
            }
        }
        return true
    }

    private fun setDepartments(userId: Int, departmentIds: List<Int>) {
        UserDepartments.deleteWhere { UserDepartments.userId eq userId }
        UserDepartments.batchInsert(departmentIds) { departmentId ->
            this[UserDepartments.userId] = userId
            this[UserDepartments.departmentId] = departmentId
        }
    }
}
