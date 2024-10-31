package no.nav.modiacontextholder

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.routing.*
import no.nav.modiacontextholder.routes.contextRoutes
import no.nav.modiacontextholder.routes.decoratorRoutes
import no.nav.modiacontextholder.routes.featureToggleRoutes
import no.nav.modiacontextholder.routes.fnrCodeExchageRoutes
import no.nav.modiacontextholder.routes.swaggerRoutes
import no.nav.modiacontextholder.utils.mdcUserId
import no.nav.personoversikt.common.ktor.utils.Security

fun Application.setupApi(security: Security) {
    routing {
        authenticate(*security.authproviders) {
            route("/api") {
                install(mdcUserId)

                decoratorRoutes()
                contextRoutes()
            }
        }

        authenticate(*security.authproviders, optional = true) {
            install(mdcUserId)

            fnrCodeExchageRoutes()
            featureToggleRoutes()
        }
        swaggerRoutes()
    }
}
