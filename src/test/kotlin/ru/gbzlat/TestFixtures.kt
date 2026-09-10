package ru.gbzlat

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import ru.gbzlat.dto.AuthRequest
import ru.gbzlat.dto.AuthResponse
import ru.gbzlat.dto.UserDTO
import ru.gbzlat.dto.UserResponse

const val ADMIN_LOGIN = "admin"
const val ADMIN_PASSWORD = "admin"

suspend fun HttpClient.login(login: String, password: String): AuthResponse =
    post("/api/auth") {
        contentType(ContentType.Application.Json)
        setBody(AuthRequest(login, password))
    }.body()

suspend fun HttpClient.adminToken(): String = login(ADMIN_LOGIN, ADMIN_PASSWORD).token

fun HttpRequestBuilder.auth(token: String) = header(HttpHeaders.Authorization, "Bearer $token")

fun HttpRequestBuilder.json(body: Any) {
    contentType(ContentType.Application.Json)
    setBody(body)
}

fun newUser(
    login: String,
    roleId: Int,
    password: String = "secret",
    departmentIds: List<Int> = listOf(1),
) = UserDTO(
    name = "Тест $login",
    login = login,
    password = password,
    roleId = roleId,
    departmentIds = departmentIds,
    phone = null,
    tgChatId = null,
)

/** Creates a user through the API and returns it along with a token for that account. */
suspend fun HttpClient.createUserAndLogin(login: String, roleId: Int): Pair<UserResponse, String> {
    val admin = adminToken()
    val created: UserResponse = post("/api/users") {
        auth(admin)
        json(newUser(login, roleId))
    }.body()

    return created to login(login, "secret").token
}
