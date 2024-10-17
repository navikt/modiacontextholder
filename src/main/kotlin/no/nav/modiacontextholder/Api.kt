package no.nav.modiacontextholder

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.routing.*
import no.nav.common.log.MDCConstants
import no.nav.modiacontextholder.routes.contextRoutes
import no.nav.modiacontextholder.routes.decoratorRoutes
import no.nav.modiacontextholder.routes.featureToggleRoutes
import no.nav.modiacontextholder.routes.fnrCodeExchageRoutes
import no.nav.modiacontextholder.routes.swaggerRoutes
import no.nav.modiacontextholder.utils.getIdent
import no.nav.personoversikt.common.ktor.utils.Security
import org.slf4j.MDC

fun Application.setupApi(security: Security) {
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

        authenticate(*security.authproviders, optional = true) {
            intercept(ApplicationCallPipeline.Call) {
                runCatching {
                    call.getIdent()
                }.onSuccess {
                    MDC.put(MDCConstants.MDC_USER_ID, it)
                }
            }
            fnrCodeExchageRoutes()
            featureToggleRoutes()
        }
        swaggerRoutes()
    }
}
