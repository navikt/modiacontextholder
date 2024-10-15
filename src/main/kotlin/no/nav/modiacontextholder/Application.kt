package no.nav.modiacontextholder

import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.application.log
import io.ktor.server.auth.Authentication
import io.micrometer.prometheusmetrics.PrometheusConfig
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry
import no.nav.modiacontextholder.AppModule.appModule
import no.nav.modiacontextholder.config.Configuration
import no.nav.modiacontextholder.mock.mockModule
import no.nav.personoversikt.common.ktor.utils.Security
import org.koin.dsl.module
import org.koin.ktor.ext.getKoin
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger

val metricsRegistry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)

fun Application.modiacontextholderApp(
    configuration: Configuration,
    useMock: Boolean = false,
) {
    install(Koin) {
        slf4jLogger()
        modules(
            if (useMock) mockModule else AppModule.externalModules,
            appModule,
            module {
                single { configuration }
            },
        )
    }
    val security =
        Security(
            configuration.azuread,
        )

    install(Authentication) {
        if (useMock) {
            security.setupMock(this, "Z999999")
        } else {
            security.setupJWT(this)
        }
    }

    setupApi(security)
    setupRedirect(security)
    setupWebsocket()
    setupInfrastructure()

    Runtime.getRuntime().addShutdownHook(
        Thread {
            log.info("Shutdown hook called, shutting down gracefully")
            getKoin().close()
        },
    )
}
