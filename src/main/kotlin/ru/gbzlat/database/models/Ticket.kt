package ru.gbzlat.database.models

import org.ktorm.entity.Entity
import org.ktorm.schema.Table
import org.ktorm.schema.int
import org.ktorm.schema.varchar

data class Ticket (
    val id: Int,
    val name: String,
    val login: String,
    val password: String,
    val roleId: Int,
    val divisionId: Int,
    val subdivisionId: Int
)

/*
object TicketTable: Table<UserEntity>("Users"){
    val id = int("id").primaryKey().bindTo { it.id }
    val name = varchar("name").bindTo { it.name }
    val login = varchar("login").bindTo { it.login }
    val password = varchar("password").bindTo { it.password }
    val roleId = int("role_id").bindTo { it.roleId }
    val divisionId = int("division_id").bindTo { it.divisionId }
    val subdivisionId = int("subdivision_id").bindTo { it.subdivisionId }
}

interface TicketEntity: Entity<UserEntity> {
    companion object : Entity.Factory<UserEntity>()

    val id: Int
    val name: String
    val login: String
    val password: String
    val roleId: Int
    val divisionId: Int
    val subdivisionId: Int
}*/
