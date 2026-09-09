package ru.gbzlat.dto

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
    val createdAt: String,
    val closedAt: String?,
    val timeLimit: String,
)

data class TicketCommentResponse(
    val id: Int,
    val ticketId: Int,
    val creator: UserResponse,
    val text: String,
    val createdAt: String,
)

data class Page<T>(
    val items: List<T>,
    val total: Long,
)

data class ErrorResponse(
    val status: Int,
    val message: String,
)

data class LoginAvailability(
    val available: Boolean,
)

data class ImportResult(
    val imported: Int,
)
