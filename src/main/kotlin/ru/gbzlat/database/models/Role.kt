package ru.gbzlat.database.models

import org.ktorm.database.Database
import org.ktorm.entity.Entity
import org.ktorm.entity.sequenceOf
import org.ktorm.schema.*
import ru.gbzlat.database

interface Role : Entity<Role> {
    companion object : Entity.Factory<Role>()

    val id: Int
    var name: String
}

object Roles : Table<Role>("Roles") {
    val id = int("id").primaryKey().bindTo { it.id }
    val name = varchar("name").bindTo { it.name }

    val Database.roles get() = database.sequenceOf(Roles)
}

/*@Serializable
data class Role (
    val id: Int,
    val name: String
)

object Roles: BaseTable<Role>("Roles") {
    val id = int("id").primaryKey()
    val name = varchar("name")

    override fun doCreateEntity(row: QueryRowSet, withReferences: Boolean)= Role(
        id = row[id]!!,
        name = row[name]!!
    )
}*/

