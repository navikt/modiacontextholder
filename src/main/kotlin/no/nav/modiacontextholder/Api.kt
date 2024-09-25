package no.nav.modiacontextholder

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.routing.*
import no.nav.common.log.MDCConstants
import no.nav.modiacontextholder.config.Configuration
import no.nav.modiacontextholder.routes.contextRoutes
import no.nav.modiacontextholder.routes.decoratorRoutes
import no.nav.modiacontextholder.utils.getIdent
import no.nav.personoversikt.common.ktor.utils.Security
import org.koin.ktor.ext.inject
import org.slf4j.MDC

fun Application.setupApi(useMock: Boolean = false) {
    val configuration: Configuration by inject()

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

    routing {
        authenticate(*security.authproviders) {
            route("/api") {
                intercept(ApplicationCallPipeline.Call) {
                    MDC.put(MDCConstants.MDC_USER_ID, call.getIdent())
                }
                decoratorRoutes()
                contextRoutes()
            }
        }
    }
}

data class UUIDPrincipal(
    val uuid: String,
) : Principal
