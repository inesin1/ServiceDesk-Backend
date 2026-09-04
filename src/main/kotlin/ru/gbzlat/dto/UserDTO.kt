package ru.gbzlat.dto


data class UserDTO(
    val name: String,
    val login: String,
    val password: String,
    val roleId: Int,
    val departmentIds: List<Int>,
    val phone: String?,
    val tgChatId: Long?
)
