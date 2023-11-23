package ru.gbzlat.dto

import kotlinx.serialization.Serializable

@Serializable
data class AuthRequest (
    val login: String,
    val password: String
)