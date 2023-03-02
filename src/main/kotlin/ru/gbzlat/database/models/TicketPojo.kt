package ru.gbzlat.database.models

import kotlinx.serialization.Serializable

@Serializable
data class TicketPojo(
    val text: String,
    val priorityId: Int,
)
