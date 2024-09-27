package no.nav.modiacontextholder

import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.micrometer.prometheusmetrics.PrometheusConfig
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry
import no.nav.modiacontextholder.AppModule.appModule
import no.nav.modiacontextholder.config.Configuration
import no.nav.modiacontextholder.mock.mockModule
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

    install(ContentNegotiation) {
        json()
    }

    setupApi(useMock)
    setupWebsocket()
    setupInfrastructure()

    Runtime.getRuntime().addShutdownHook(
        Thread {
            log.info("Shutdown hook called, shutting down gracefully")
            getKoin().close()
        },
    )
}
