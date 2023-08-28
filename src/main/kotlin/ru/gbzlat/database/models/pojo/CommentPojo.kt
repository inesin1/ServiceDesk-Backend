package ru.gbzlat.database.models.pojo

import kotlinx.serialization.Serializable

@Serializable
data class CommentPojo (
    val ticketId: Int,
    val userId: Int,
    val text: String
)