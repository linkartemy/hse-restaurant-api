package com.restaurant.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.defaultheaders.*
import io.ktor.server.plugins.openapi.*
import io.ktor.server.plugins.swagger.*
import io.ktor.server.routing.*


fun Application.configureHTTP() {
    routing {
        swaggerUI(path = "openapi", swaggerFile = "openapi/documentation.yaml")
    }
    routing {
        openAPI(path = "openapi", swaggerFile = "openapi/documentation.yaml")
    }
    install(DefaultHeaders) {
        header("X-Engine", "Ktor")
    }
    install(CORS) {
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Patch)
        allowHeader(HttpHeaders.Authorization)
        anyHost()
        allowHeader(HttpHeaders.ContentType)
    }
}
