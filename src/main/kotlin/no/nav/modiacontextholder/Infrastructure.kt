package no.nav.modiacontextholder

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import no.nav.modiacontextholder.utils.AuthorizationException
import no.nav.modiacontextholder.utils.HTTPException
import no.nav.modiacontextholder.utils.getAuthorizedParty
import no.nav.personoversikt.common.ktor.utils.Metrics
import no.nav.personoversikt.common.ktor.utils.Selftest

fun Application.setupInfrastructure() {
    install(CORS) {
        anyHost()
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Delete)
        allowCredentials
        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.ContentType)
    }

    install(Metrics.Plugin) {
        metricsRegistry
        timers { call, exception ->
            tag("authorized_party", call.getAuthorizedParty().orElse(""))
        }
    }

    install(Selftest.Plugin) {
        appname = APP_NAME
        version = appImage
    }

    install(StatusPages) {
        exception<Throwable> { call, cause ->
            println(cause.message)
            println(cause.stackTraceToString())
            logger.error(cause.message, cause.stackTrace)
            if (cause is HTTPException) {
                call.respondText(text = "${cause.statusCode()}: $cause}", status = cause.statusCode())
            }
            if (cause is AuthorizationException) {
                call.respondText(text = "403: $cause", status = HttpStatusCode.Forbidden)
            } else {
                call.respondText(text = "500: $cause", status = HttpStatusCode.InternalServerError)
            }
        }
    }

    install(CallLogging)
}
