package ru.gbzlat.database.models

import org.ktorm.database.Database
import org.ktorm.dsl.eq
import org.ktorm.entity.Entity
import org.ktorm.entity.filter
import org.ktorm.entity.map
import org.ktorm.entity.sequenceOf
import org.ktorm.schema.*
import ru.gbzlat.database
import ru.gbzlat.database.models.UserDepartments.userDepartments

interface User : Entity<User> {
    companion object : Entity.Factory<User>()

    var id: Int
    var name: String
    var login: String
    var password: String
    var role: Role
    var departments: List<Department>?
    var phone: String?
    var token: String?
    var tgChatId: Long?

    // Инициализирует дополнительные поля
    fun addDepartments() {
        departments = database.userDepartments
            .filter { it.userId eq id }
            .map { it.department }
    }
}

object Users : Table<User>("Users") {
    val id = int("id").primaryKey().bindTo { it.id }
    val name = varchar("name").bindTo { it.name }
    val login = varchar("login").bindTo { it.login }
    val password = varchar("password").bindTo { it.password }
    val roleId = int("role_id").references(Roles) { it.role }
    val phone = varchar("phone").bindTo { it.phone }
    val token = varchar("token").bindTo { it.token }
    val tgChatId = long("tg_chat_id").bindTo { it.tgChatId }

    val Database.users get() = database.sequenceOf(Users)
}

/*data class User (
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
}*/