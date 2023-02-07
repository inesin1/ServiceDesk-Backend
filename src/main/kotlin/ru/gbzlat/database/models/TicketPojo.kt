package ru.gbzlat.database.models

import kotlinx.serialization.Serializable

@Serializable
data class TicketPojo(
    val userId: Int,
    val text: String,
    val priorityId: Int,
    val statusId: Int,
)
