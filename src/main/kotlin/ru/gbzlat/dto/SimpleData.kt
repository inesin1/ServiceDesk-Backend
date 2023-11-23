package ru.gbzlat.dto

import kotlinx.serialization.Serializable

// Простые данные (name)
@Serializable
data class SimpleData(
    val name: String
)
