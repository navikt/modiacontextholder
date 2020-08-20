package no.nav.sbl

import Configuration
import Security
import eventRoutes
import exceptionHandler
import javax.sql.DataSource
import io.ktor.application.Application
import io.ktor.application.install
import io.ktor.auth.Authentication
import io.ktor.features.CORS
import io.ktor.features.CallLogging
import io.ktor.features.ContentNegotiation
import io.ktor.jackson.JacksonConverter
import io.ktor.features.StatusPages
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.metrics.micrometer.MicrometerMetrics
import io.ktor.request.path
import io.ktor.routing.route
import io.ktor.routing.routing
import io.micrometer.prometheus.PrometheusConfig
import io.micrometer.prometheus.PrometheusMeterRegistry
import no.nav.sbl.JacksonUtils.objectMapper
import no.nav.sbl.dao.EventDAOImpl
import notFoundHandler
import org.slf4j.event.Level
import setupJWT


val metricsRegistry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)
data class ApplicationState(var running: Boolean = true, var initialized: Boolean = false)

fun Application.modiacontextholder(
        configuration: Configuration,
        dataSource: DataSource
){

    install(StatusPages){
        notFoundHandler()
        exceptionHandler()
    }
    install(CORS){
        anyHost()
        method(HttpMethod.Get)
        method(HttpMethod.Post)
        method(HttpMethod.Delete)
    }
    install(Authentication){
        setupJWT(configuration.jwksUrl)
    }

    install(ContentNegotiation){
        register(ContentType.Application.Json, JacksonConverter(objectMapper))
    }

    install(CallLogging) {
        level = Level.INFO
        filter { call -> call.request.path().startsWith("/modiapersonoversikt-draft/api") }
        mdc("userId", Security::getSubject)
    }

    install(MicrometerMetrics) {
        registry = metricsRegistry
    }

    val eventDAO = EventDAOImpl(dataSource)
    routing{
        route("modiacontextholder"){
            eventRoutes(eventDAO)
        }
    }


}