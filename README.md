# Service Desk — backend

API for an internal IT helpdesk: employees file tickets, IT specialists pick them up and close them, administrators manage users and reference data. Written for a clinic, but nothing in it is domain-specific.

Ktor 3 on JDK 21, PostgreSQL through Exposed, schema managed by Flyway. Optionally notifies IT staff through a Telegram bot.

The web client lives in a separate repository: [ServiceDesk-Client-Vue](https://github.com/inesin1/ServiceDesk-Client-Vue).

## Running

```
cp .env.example .env
docker compose up
```

That brings up PostgreSQL and the API on port 1002. Flyway creates the schema on first boot and seeds the reference tables plus an `admin` / `admin` account — change the password before exposing it anywhere.

To run the API outside Docker against the database from compose:

```
docker compose up -d db
DB_PASSWORD=postgres JWT_SECRET=dev-secret ./gradlew run
```

## Configuration

All settings come from the environment; see `.env.example`.

| | | |
|---|---|---|
| `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER` | `localhost`, `5432`, `service_desk`, `postgres` | database connection |
| `DB_PASSWORD` | — | required |
| `JWT_SECRET` | — | required, signs access tokens |
| `JWT_TTL_HOURS` | `12` | token lifetime |
| `CORS_ALLOWED_HOSTS` | `http://localhost:10011` | comma-separated origins |
| `TGBOT_TOKEN` | — | optional; notifications are disabled when unset |

## API

With the server running, the OpenAPI specification is at `/api.json` and Swagger UI at `/swagger`.

Authentication is a bearer JWT from `POST /api/auth`. The token carries the user id and role. Roles are `1` employee, `2` IT specialist, `3` administrator: employees only ever see their own tickets, specialists can take tickets into work and close them, and administrators manage users and reference data.

## Commands

| | |
|---|---|
| `./gradlew run` | start the API |
| `./gradlew test` | integration tests against PostgreSQL in Testcontainers |
| `./gradlew buildFatJar` | build a self-contained jar |
