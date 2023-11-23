package ru.gbzlat.dto

import ru.gbzlat.database.models.User

data class AuthResponse (
    val user: User,
    val token: String
)