package ru.gbzlat.dto

import java.time.LocalDateTime

data class Ref(
    val id: Int,
    val name: String,
)

data class UserResponse(
    val id: Int,
    val name: String,
    val login: String,
    val role: Ref,
    val phone: String?,
    val tgChatId: Long?,
    val departments: List<Ref>?,
)

data class TicketResponse(
    val id: Int,
    val creatorId: Int,
    val creator: UserResponse,
    val executorId: Int?,
    val executor: UserResponse?,
    val details: String?,
    val source: Ref,
    val category: Ref,
    val status: Ref,
    val createdAt: LocalDateTime,
    val closedAt: LocalDateTime?,
    val timeLimit: LocalDateTime,
)

data class TicketCommentResponse(
    val id: Int,
    val ticketId: Int,
    val creator: UserResponse,
    val text: String,
    val createdAt: LocalDateTime,
)

data class Page<T>(
    val items: List<T>,
    val total: Long,
)
