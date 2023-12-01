package ru.gbzlat.dto

import kotlinx.serialization.Serializable

@Serializable
data class TicketDTO(
    val creatorId: Int,
    val sourceId: Int,
    val categoryId: Int,
    val details: String?,
)
