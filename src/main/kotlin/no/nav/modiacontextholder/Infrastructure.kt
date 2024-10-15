package no.nav.modiacontextholder

import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.request.path
import io.ktor.server.response.respondText
import io.ktor.server.routing.IgnoreTrailingSlash
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import no.nav.common.health.selftest.SelfTestUtils
import no.nav.common.health.selftest.SelftestHtmlGenerator
import no.nav.modiacontextholder.infrastructur.HealthCheckAware
import no.nav.modiacontextholder.utils.AuthorizationException
import no.nav.modiacontextholder.utils.HTTPException
import no.nav.modiacontextholder.utils.getAuthorizedParty
import no.nav.personoversikt.common.ktor.utils.Metrics
import no.nav.personoversikt.common.ktor.utils.Selftest
import org.koin.ktor.ext.getKoin
import kotlin.concurrent.fixedRateTimer
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

fun Application.setupInfrastructure() {
    install(ContentNegotiation) {
        json()
    }

    install(IgnoreTrailingSlash)

    install(CORS) {
        anyHost()
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Delete)
        allowCredentials = true
        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.ContentType)
    }

    install(Metrics.Plugin) {
        registry = metricsRegistry
        timers { call, _ ->
            tag("authorized_party", call.getAuthorizedParty().orElse(""))
        }
    }

    fixedRateTimer(
        name = "Healthchecks",
        daemon = false,
        period = 5.minutes.inWholeMilliseconds,
        initialDelay = 30.seconds.inWholeMilliseconds,
    ) {
        val selfTests = getKoin().getAll<HealthCheckAware>().map { it.getHealthCheck() }
        SelfTestUtils.checkAllParallel(selfTests)
    }
    routing {
        route("/internal/health") {
            val selfTests = getKoin().getAll<HealthCheckAware>().map { it.getHealthCheck() }
            get {
                val result = SelfTestUtils.checkAllParallel(selfTests)
                val markup = SelftestHtmlGenerator.generate(result)
                call.respondText(markup, contentType = ContentType.Text.Html)
            }
        }
    }

    install(Selftest.Plugin) {
        appname = APP_NAME
        version = appImage
    }

    install(StatusPages) {
        exception<Throwable> { call, cause ->
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

    install(CallLogging) {
        filter { call ->
            !call.request.path().startsWith("/internal")
        }
    }
}
