package ru.gbzlat.dto

data class AuthResponse(
    val user: UserResponse,
    val token: String,
)
