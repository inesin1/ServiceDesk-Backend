package ru.gbzlat.database.models

import org.ktorm.dsl.QueryRowSet
import org.ktorm.entity.sequenceOf
import org.ktorm.entity.toList
import org.ktorm.schema.BaseTable
import org.ktorm.schema.int
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
    val tgChatId: String?,

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
    val tgChatId = varchar("tg_chat_id")

    override fun doCreateEntity(row: QueryRowSet, withReferences: Boolean)= User(
        id = row[id] ?: 0,
        name = row[name].orEmpty(),
        login = row[login].orEmpty(),
        password = row[password].orEmpty(),
        roleId = row[roleId] ?: 0,
        departmentId = row[departmentId] ?: 0,
        phoneNumber = row[phoneNumber].orEmpty(),
        token = row[token].orEmpty(),
        tgChatId = row[tgChatId].orEmpty(),

        role = database.roles.toList().single { it.id == row[roleId] },
        department = database.departments.toList().single { it.id == row[departmentId] }
    )
}

val DatabaseManager.users get() = database.sequenceOf(Users)

/*
object UserTable: Table<UserEntity>("Users"){
    val id = int("id").primaryKey().bindTo { it.id }
    val name = varchar("name").bindTo { it.name }
    val login = varchar("login").bindTo { it.login }
    val password = varchar("password").bindTo { it.password }
    val roleId = int("role_id").bindTo { it.roleId }
    val divisionId = int("division_id").bindTo { it.divisionId }
    val subdivisionId = int("subdivision_id").bindTo { it.subdivisionId }
}

interface UserEntity: Entity<UserEntity> {
    companion object : Entity.Factory<UserEntity>()

    val id: Int
    val name: String
    val login: String
    val password: String
    val roleId: Int
    val divisionId: Int
    val subdivisionId: Int
}*/
