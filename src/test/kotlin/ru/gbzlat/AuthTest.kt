package ru.gbzlat

import com.auth0.jwt.JWT
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class AuthTest {

    @Test
    fun `пускает засеянного администратора по bcrypt-хешу`() = withApp { client ->
        val response = client.login(ADMIN_LOGIN, ADMIN_PASSWORD)

        assertEquals(ADMIN_LOGIN, response.user.login)
        assertEquals(3, response.user.role.id)
    }

    @Test
    fun `кладёт роль и срок жизни в токен`() = withApp { client ->
        val payload = JWT.decode(client.adminToken())

        assertEquals(3, payload.getClaim("role").asInt())
        assertEquals(1, payload.getClaim("id").asInt())
        assertNotNull(payload.expiresAt)
    }

    @Test
    fun `отвечает 401 на неверный пароль`() = withApp { client ->
        val response = client.post("/api/auth") { json(mapOf("login" to ADMIN_LOGIN, "password" to "wrong")) }

        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `отвечает 401 на несуществующий логин`() = withApp { client ->
        val response = client.post("/api/auth") { json(mapOf("login" to "нет-такого", "password" to "x")) }

        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `не отдаёт пароль в ответе`() = withApp { client ->
        val body = client.post("/api/auth") {
            json(mapOf("login" to ADMIN_LOGIN, "password" to ADMIN_PASSWORD))
        }

        assertNull(Regex("\"password\"").find(body.bodyAsText()))
    }

    @Test
    fun `отвечает 401 без токена`() = withApp { client ->
        assertEquals(HttpStatusCode.Unauthorized, client.get("/api/users").status)
    }
}
