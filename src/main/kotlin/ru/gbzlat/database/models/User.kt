package ru.gbzlat.database.models

import org.ktorm.dsl.QueryRowSet
import org.ktorm.dsl.eq
import org.ktorm.entity.find
import org.ktorm.entity.sequenceOf
import org.ktorm.entity.toList
import org.ktorm.schema.BaseTable
import org.ktorm.schema.int
import org.ktorm.schema.long
import org.ktorm.schema.varchar
import ru.gbzlat.database.DatabaseManager
import ru.gbzlat.database

data class User (
    val id: Int,
    val name: String,
    val login: String,
    val password: String,
    val roleId: Int,
    val departmentId: Int,
    val phoneNumber: String?,
    val token: String?,
    val tgChatId: Long?,

    val role: Role?,
    val department: Department?
)

object Users: BaseTable<User>("Users") {
    val id = int("id").primaryKey()
    val name = varchar("name")
    val login = varchar("login")
    val password = varchar("password")
    val roleId = int("role_id")
    val departmentId = int("department_id")
    val phoneNumber = varchar("phone_number")
    val token = varchar("token")
    val tgChatId = long("tg_chat_id")

    override fun doCreateEntity(row: QueryRowSet, withReferences: Boolean)= User(
        id = row[id] ?: 0,
        name = row[name].orEmpty(),
        login = row[login].orEmpty(),
        password = row[password].orEmpty(),
        roleId = row[roleId] ?: 0,
        departmentId = row[departmentId] ?: 0,
        phoneNumber = row[phoneNumber].orEmpty(),
        token = row[token].orEmpty(),
        tgChatId = row[tgChatId],

        role = database.roles.find { it.id eq row[roleId]!!.toInt() },
        department = database.departments.find { it.id eq row[departmentId]!!.toInt() }
    )
}

val DatabaseManager.users get() = database.sequenceOf(Users)