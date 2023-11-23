package ru.gbzlat.dto

import kotlinx.serialization.Serializable

@Serializable
data class TicketDTO(
    val categoryId: Int,
    val details: String?,
)
