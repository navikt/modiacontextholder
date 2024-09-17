package no.nav.modiacontextholder

import io.getunleash.UnleashContextProvider
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.lettuce.core.RedisClient
import io.micrometer.prometheusmetrics.PrometheusConfig
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry
import no.nav.modiacontextholder.AppModule.appModule
import no.nav.modiacontextholder.config.Configuration
import no.nav.modiacontextholder.redis.setupRedisConsumer
import no.nav.modiacontextholder.routes.contextRoutes
import no.nav.modiacontextholder.routes.decoratorRoutes
import no.nav.modiacontextholder.routes.naisRoutes
import no.nav.modiacontextholder.utils.AuthorizationException
import no.nav.modiacontextholder.utils.HTTPException
import no.nav.modiacontextholder.utils.WebsocketStorage
import no.nav.personoversikt.common.ktor.utils.Metrics
import no.nav.personoversikt.common.ktor.utils.Security
import no.nav.personoversikt.common.ktor.utils.Selftest
import org.koin.ktor.ext.getKoin
import org.koin.ktor.plugin.Koin
import org.koin.ktor.plugin.scope
import org.koin.logger.slf4jLogger
import java.time.Duration

data class ApplicationState(
    var running: Boolean = true,
    var initialized: Boolean = false,
)

val metricsRegistry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)

fun Application.modiacontextholderApp(
    configuration: Configuration,
    useMock: Boolean = false,
) {
    val applicationState = ApplicationState()

    val redisClient = RedisClient.create(configuration.redisUri)

    val redisConsumer = setupRedisConsumer(redisClient)
    val websocketStorage = WebsocketStorage(redisConsumer.getFlow())
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

    if (!useMock) {
        install(Koin) {
            slf4jLogger()
            modules(appModule(redisClient))
        }
    }

    install(Metrics.Plugin) {
        metricsRegistry
    }

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
                intercept(ApplicationCallPipeline.Call) {
                    call.scope.get<UnleashContextProvider> {
                        org.koin.core.parameter
                            .parametersOf(call)
                    }
                }
                decoratorRoutes()
                contextRoutes()
            }
        }

        naisRoutes()

        webSocket(path = "/ws/{ident}", handler = websocketStorage.wsHandler)
    }

    redisConsumer.start()
    applicationState.initialized = true

    Runtime.getRuntime().addShutdownHook(
        Thread {
            log.info("Shutdown hook called, shutting down gracefully")
            redisConsumer.stop()
            getKoin().close()
            redisClient.close()
            applicationState.running = false
        },
    )
}

data class UUIDPrincipal(
    val uuid: String,
) : Principal
