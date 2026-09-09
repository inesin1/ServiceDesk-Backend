package ru.gbzlat.plugins

import io.github.smiley4.ktoropenapi.OpenApi
import io.github.smiley4.ktoropenapi.config.AuthScheme
import io.github.smiley4.ktoropenapi.config.AuthType
import io.github.smiley4.ktoropenapi.config.SchemaGenerator
import io.github.smiley4.schemakenerator.swagger.data.RefType
import io.github.smiley4.schemakenerator.swagger.data.TitleType
import io.ktor.server.application.*
import ru.gbzlat.security.RoleRouteSelector

fun Application.configureOpenApi() {
    install(OpenApi) {
        ignoredRouteSelectors = setOf(RoleRouteSelector::class)
        schemas {
            generator = SchemaGenerator.reflection {
                title = TitleType.SIMPLE
                referencePath = RefType.OPENAPI_SIMPLE
            }
        }
        info {
            title = "Service Desk API"
            version = "2.0.0"
            description = "Учёт заявок в ИТ-отдел."
            license {
                name = "MIT"
                identifier = "MIT"
            }
        }
        server {
            url = "http://localhost:1002"
            description = "Локальная разработка"
        }
        security {
            securityScheme("bearerAuth") {
                type = AuthType.HTTP
                scheme = AuthScheme.BEARER
                bearerFormat = "JWT"
            }
            defaultSecuritySchemeNames("bearerAuth")
            defaultUnauthorizedResponse {
                description = "Токен отсутствует, истёк или подписан другим ключом"
            }
        }
    }
}
