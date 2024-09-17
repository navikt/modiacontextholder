package no.nav.modiacontextholder.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import no.nav.common.health.selftest.SelfTestUtils
import no.nav.common.health.selftest.SelftestHtmlGenerator
import no.nav.modiacontextholder.infrastructur.HealthCheckAware
import no.nav.modiacontextholder.metricsRegistry
import org.koin.ktor.ext.getKoin

fun Route.naisRoutes() {
    val checkableServices = getKoin().getAll<HealthCheckAware>()

    route("/internal") {
        get("/isalive") {
        }
        get("/isready") {
        }

        get("/metrics") {
            call.respondText(metricsRegistry.scrape())
        }

        get("/seltest") {
            withContext(Dispatchers.IO) {
                val selftest = SelfTestUtils.checkAllParallel(checkableServices.map { it.getHealthCheck() })
                val markup = SelftestHtmlGenerator.generate(selftest)

                call.respondText(markup, ContentType.Text.Html)
            }
        }
    }
}
