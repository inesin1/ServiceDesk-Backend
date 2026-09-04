package ru.gbzlat.dto


data class TicketDTO(
    val creatorId: Int,
    val sourceId: Int,
    val categoryId: Int,
    val details: String?,
)
