package ru.gbzlat.database.models

import kotlinx.serialization.Serializable

@Serializable
data class Auth (
    val login: String,
    val password: String
)