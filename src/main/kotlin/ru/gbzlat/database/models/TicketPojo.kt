package ru.gbzlat.database.models

import kotlinx.serialization.Serializable

@Serializable
data class TicketPojo(
    val creatorId: Int,
    val executorId: Int,
    val text: String,
    val priorityId: Int,
    val statusId: Int,
)
