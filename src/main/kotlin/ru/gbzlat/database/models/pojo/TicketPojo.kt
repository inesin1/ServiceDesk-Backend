package ru.gbzlat.database.models

import kotlinx.serialization.Serializable

@Serializable
data class TicketPojo(
    val categoryId: Int,
    val details: String?,
)
