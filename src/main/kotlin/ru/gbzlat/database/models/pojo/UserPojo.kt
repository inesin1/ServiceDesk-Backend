package ru.gbzlat.database.models.pojo

import kotlinx.serialization.Serializable

@Serializable
data class UserPojo(
    val name: String,
    val login: String,
    val password: String,
    val roleId: Int,
    val departmentId: Int,
    val phoneNumber: String?,
    val tgChatId: Long?
)
