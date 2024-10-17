package no.nav.modiacontextholder.routes

import io.ktor.server.plugins.swagger.swaggerUI
import io.ktor.server.routing.Route

fun Route.swaggerRoutes() {
    swaggerUI(path = "swagger", swaggerFile = "openapi/documentation.yaml")
}
