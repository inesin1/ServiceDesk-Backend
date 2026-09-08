package ru.gbzlat.service

import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import ru.gbzlat.db.Users
import ru.gbzlat.db.loadUsers
import ru.gbzlat.dto.UserResponse
import ru.gbzlat.security.Role
import ru.gbzlat.security.verifyPassword

object AuthService {

    fun authenticate(login: String, password: String): Pair<UserResponse, Role>? = transaction {
        val row = Users.select(Users.id, Users.password)
            .where { Users.login eq login }
            .singleOrNull()
            ?: return@transaction null

        if (!verifyPassword(password, row[Users.password])) return@transaction null

        val id = row[Users.id]
        val user = loadUsers(listOf(id), withDepartments = true).getValue(id)
        user to Role.byId(user.role.id)!!
    }
}
