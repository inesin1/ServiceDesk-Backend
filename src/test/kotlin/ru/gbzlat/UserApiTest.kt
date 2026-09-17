package ru.gbzlat

import io.ktor.client.request.*
import io.ktor.http.*
import kotlin.test.Test
import kotlin.test.assertEquals

class UserApiTest {

    private suspend fun io.ktor.client.HttpClient.loginStatus(login: String, password: String) =
        post("/api/auth") { json(mapOf("login" to login, "password" to password)) }.status

    @Test
    fun `не меняет пароль, если его нет в теле`() = withApp { client ->
        val (user, token) = client.createUserAndLogin("emp-keep-pass", 1)

        val response = client.put("/api/users/${user.id}") {
            auth(token)
            json(newUser("emp-keep-pass", 1, password = null))
        }

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(HttpStatusCode.OK, client.loginStatus("emp-keep-pass", "secret"))
    }

    @Test
    fun `меняет пароль, если он передан`() = withApp { client ->
        val (user, token) = client.createUserAndLogin("emp-new-pass", 1)

        client.put("/api/users/${user.id}") {
            auth(token)
            json(newUser("emp-new-pass", 1, password = "changed"))
        }

        assertEquals(HttpStatusCode.Unauthorized, client.loginStatus("emp-new-pass", "secret"))
        assertEquals(HttpStatusCode.OK, client.loginStatus("emp-new-pass", "changed"))
    }

    @Test
    fun `отвечает 400 на пустой пароль при правке`() = withApp { client ->
        val (user, token) = client.createUserAndLogin("emp-blank-pass", 1)

        val response = client.put("/api/users/${user.id}") {
            auth(token)
            json(newUser("emp-blank-pass", 1, password = " "))
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertEquals(HttpStatusCode.OK, client.loginStatus("emp-blank-pass", "secret"))
    }

    @Test
    fun `отвечает 400 на создание без пароля`() = withApp { client ->
        val response = client.post("/api/users") {
            auth(client.adminToken())
            json(newUser("no-pass", 1, password = null))
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
    }
}
