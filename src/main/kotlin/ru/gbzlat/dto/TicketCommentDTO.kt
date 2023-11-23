package ru.gbzlat.dto

import kotlinx.serialization.Serializable

@Serializable
data class TicketCommentDTO (
    val creatorId: Int,
    val text: String
)