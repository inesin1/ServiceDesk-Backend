package ru.gbzlat.database.models

import org.ktorm.database.Database
import org.ktorm.entity.Entity
import org.ktorm.entity.sequenceOf
import org.ktorm.schema.*
import ru.gbzlat.database

interface UserDepartment : Entity<UserDepartment> {
    companion object : Entity.Factory<UserDepartment>()

    var user: User
    var department: Department
}

object UserDepartments : Table<UserDepartment>("User_Departments") {
    val userId = int("user_id").primaryKey().references(Users) { it.user }
    val departmentId = int("department_id").primaryKey().references(Departments) { it.department }

    val Database.userDepartments get() = database.sequenceOf(UserDepartments)
}

/*@Serializable
data class Status (
    val id: Int,
    val name: String
)

object Statuses: BaseTable<Status>("Statuses") {
    val id = int("id").primaryKey()
    val name = varchar("name")

    override fun doCreateEntity(row: QueryRowSet, withReferences: Boolean)= Status(
        id = row[id]!!,
        name = row[name]!!
    )
}*/

