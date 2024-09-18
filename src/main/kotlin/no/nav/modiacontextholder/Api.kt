package no.nav.modiacontextholder

import io.getunleash.UnleashContextProvider
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.routing.*
import no.nav.modiacontextholder.config.Configuration
import no.nav.modiacontextholder.routes.contextRoutes
import no.nav.modiacontextholder.routes.decoratorRoutes
import no.nav.personoversikt.common.ktor.utils.Security
import org.koin.ktor.ext.inject
import org.koin.ktor.plugin.scope

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
        basic("ws") {
            validate {
                UUIDPrincipal(it.name)
            }
        }
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
    }
}

data class UUIDPrincipal(
    val uuid: String,
) : Principal
