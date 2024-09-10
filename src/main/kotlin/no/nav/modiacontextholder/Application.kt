package no.nav.modiacontextholder

import io.ktor.http.*
import io.ktor.serialization.kotlinx.*
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.*
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import no.nav.modiacontextholder.config.Configuration
import no.nav.personoversikt.common.ktor.utils.Metrics
import no.nav.personoversikt.common.ktor.utils.Security
import no.nav.personoversikt.common.ktor.utils.Selftest
import java.time.Duration

fun Application.modiacontextholderApp(
    configuration: Configuration,
    useMock: Boolean = false,
) {
    val security =
        Security(
            configuration.azuread,
        )

    install(CORS) {
        anyHost()
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowCredentials
        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.ContentType)
    }

    install(Metrics.Plugin)

    install(Selftest.Plugin) {
        appname = APP_NAME
        version = appImage
    }

    install(Authentication) {
        if (useMock) {
            security.setupMock(this, "Z999999")
        } else {
            security.setupJWT(this)
        }
        basic("ws") {
            validate {
                UUIDPrincipal(it.name)
            }
        }
    }

    install(ContentNegotiation) {
        json()
    }

    install(WebSockets) {
        pingPeriod = Duration.ofMinutes(1)
        timeout = Duration.ofMinutes(5)
    }

    routing {
        authenticate(*security.authproviders) {
            route("/api") {
                contextRoutes()
            }
        }
    }
}

data class UUIDPrincipal(
    val uuid: String,
) : Principal
