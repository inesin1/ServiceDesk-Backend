package ru.gbzlat

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import ru.gbzlat.dto.ErrorResponse
import ru.gbzlat.dto.Page
import ru.gbzlat.dto.TicketResponse
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TicketApiTest {

    private suspend fun io.ktor.client.HttpClient.createTicket(token: String, creatorId: Int, categoryId: Int = 1) =
        post("/api/tickets") {
            auth(token)
            json(mapOf("creatorId" to creatorId, "sourceId" to 1, "categoryId" to categoryId, "details" to "x"))
        }

    @Test
    fun `подставляет автора токена вместо creatorId из тела для сотрудника`() = withApp { client ->
        val (user, token) = client.createUserAndLogin("emp-forge", 1)

        client.createTicket(token, creatorId = 1)
        val page: Page<TicketResponse> = client.get("/api/tickets") { auth(token) }.body()

        assertTrue(page.items.isNotEmpty())
        assertTrue(page.items.all { it.creatorId == user.id })
    }

    @Test
    fun `считает total с учётом фильтра`() = withApp { client ->
        val admin = client.adminToken()
        val (user, _) = client.createUserAndLogin("emp-filter", 1)
        client.createTicket(admin, user.id, categoryId = 2)
        client.createTicket(admin, user.id, categoryId = 3)

        val all: Page<TicketResponse> = client.get("/api/tickets") { auth(admin) }.body()
        val filtered: Page<TicketResponse> = client.get("/api/tickets?categories=2") { auth(admin) }.body()

        assertTrue(filtered.total < all.total)
        assertEquals(filtered.total, filtered.items.size.toLong())
        assertTrue(filtered.items.all { it.category.id == 2 })
    }

    @Test
    fun `считает total с учётом роли`() = withApp { client ->
        val admin = client.adminToken()
        val (user, token) = client.createUserAndLogin("emp-scope", 1)
        client.createTicket(admin, creatorId = 1)
        client.createTicket(admin, creatorId = user.id)

        val asAdmin: Page<TicketResponse> = client.get("/api/tickets") { auth(admin) }.body()
        val asEmployee: Page<TicketResponse> = client.get("/api/tickets") { auth(token) }.body()

        assertTrue(asEmployee.total < asAdmin.total)
        assertTrue(asEmployee.items.all { it.creatorId == user.id })
    }

    @Test
    fun `записывает комментарий на автора токена`() = withApp { client ->
        val admin = client.adminToken()
        val (user, token) = client.createUserAndLogin("emp-comment", 1)
        client.createTicket(admin, user.id)
        val ticketId: Int = client.get<Page<TicketResponse>>("/api/tickets", token).items.first().id

        client.post("/api/tickets/$ticketId/comments") { auth(token); json(mapOf("text" to "тест")) }
        val comments: List<ru.gbzlat.dto.TicketCommentResponse> =
            client.get("/api/tickets/$ticketId/comments") { auth(token) }.body()

        assertEquals(user.id, comments.last().creator.id)
    }

    @Test
    fun `отвечает 400 на нечисловой id`() = withApp { client ->
        val token = client.adminToken()

        val response = client.get("/api/tickets/abc") { auth(token) }
        val body: ErrorResponse = response.body()

        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertEquals(400, body.status)
    }

    @Test
    fun `отвечает 404 на несуществующую заявку`() = withApp { client ->
        val token = client.adminToken()

        assertEquals(HttpStatusCode.NotFound, client.get("/api/tickets/999999") { auth(token) }.status)
    }

    @Test
    fun `отвечает 409 на заявку с несуществующей категорией`() = withApp { client ->
        val token = client.adminToken()

        val response = client.createTicket(token, creatorId = 1, categoryId = 999)

        assertEquals(HttpStatusCode.Conflict, response.status)
    }

    @Test
    fun `отдаёт даты строкой`() = withApp { client ->
        val admin = client.adminToken()
        client.createTicket(admin, creatorId = 1)

        val page: Page<TicketResponse> = client.get("/api/tickets?limit=1") { auth(admin) }.body()

        assertTrue(page.items.first().createdAt.startsWith("20"))
    }
}

private suspend inline fun <reified T> io.ktor.client.HttpClient.get(url: String, token: String): T =
    get(url) { auth(token) }.body()
