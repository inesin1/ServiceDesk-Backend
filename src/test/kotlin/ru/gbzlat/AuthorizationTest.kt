package ru.gbzlat

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import ru.gbzlat.dto.SimpleData
import ru.gbzlat.dto.UserResponse
import kotlin.test.Test
import kotlin.test.assertEquals

class AuthorizationTest {

    @Test
    fun `возвращает 403 сотруднику на создании пользователя`() = withApp { client ->
        val (_, token) = client.createUserAndLogin("emp-create", 1)

        val response = client.post("/api/users") {
            auth(token)
            json(newUser("кто-угодно", 3))
        }

        assertEquals(HttpStatusCode.Forbidden, response.status)
    }

    @Test
    fun `возвращает 403 сотруднику на записи в справочник`() = withApp { client ->
        val (_, token) = client.createUserAndLogin("emp-ref", 1)

        val created = client.post("/api/tickets/statuses") { auth(token); json(SimpleData("Хак")) }
        val deleted = client.delete("/api/tickets/statuses/1") { auth(token) }

        assertEquals(HttpStatusCode.Forbidden, created.status)
        assertEquals(HttpStatusCode.Forbidden, deleted.status)
    }

    @Test
    fun `пускает сотрудника читать справочники`() = withApp { client ->
        val (_, token) = client.createUserAndLogin("emp-read", 1)

        assertEquals(HttpStatusCode.OK, client.get("/api/tickets/statuses") { auth(token) }.status)
        assertEquals(HttpStatusCode.OK, client.get("/api/departments") { auth(token) }.status)
    }

    @Test
    fun `не даёт сотруднику повысить себе роль через свой профиль`() = withApp { client ->
        val (user, token) = client.createUserAndLogin("emp-escalate", 1)

        val updated: UserResponse = client.put("/api/users/${user.id}") {
            auth(token)
            json(newUser("emp-escalate", roleId = 3, departmentIds = listOf(1, 2, 3)))
        }.body()

        assertEquals(1, updated.role.id)
        assertEquals(listOf(1), updated.departments?.map { it.id })
    }

    @Test
    fun `возвращает 403 на правку чужого профиля`() = withApp { client ->
        val (_, token) = client.createUserAndLogin("emp-other", 1)

        val response = client.put("/api/users/1") { auth(token); json(newUser("admin", 3)) }

        assertEquals(HttpStatusCode.Forbidden, response.status)
    }

    @Test
    fun `возвращает 403 сотруднику на приёме заявки в работу`() = withApp { client ->
        val (user, token) = client.createUserAndLogin("emp-work", 1)
        val admin = client.adminToken()
        client.post("/api/tickets") {
            auth(admin)
            json(mapOf("creatorId" to user.id, "sourceId" to 1, "categoryId" to 1, "details" to "x"))
        }

        val response = client.put("/api/tickets/1/work/${user.id}") { auth(token) }

        assertEquals(HttpStatusCode.Forbidden, response.status)
    }
}
