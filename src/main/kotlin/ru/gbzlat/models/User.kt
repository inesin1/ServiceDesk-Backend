package ru.gbzlat.models

data class User (
    val id: Int,
    val name: String,
    val login: String,
    val password: String,
    val roleId: Int,
    val divisionId: Int,
    val subdivisionId: Int
)