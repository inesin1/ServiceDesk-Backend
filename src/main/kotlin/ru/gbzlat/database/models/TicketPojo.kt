package ru.gbzlat.database.models

import kotlinx.serialization.Serializable

@Serializable
data class TicketPojo(
    val subject: String,
    val text: String?,
    val priorityId: Int,
)
