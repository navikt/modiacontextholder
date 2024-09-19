package no.nav.modiacontextholder.service.unleash

import io.getunleash.UnleashContextProvider
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import no.nav.common.log.MDCConstants
import no.nav.modiacontextholder.AppModule
import no.nav.modiacontextholder.mock.mockModule
import no.nav.modiacontextholder.rest.TestApplication
import no.nav.modiacontextholder.utils.getIdent
import no.nav.personoversikt.common.ktor.utils.Security
import org.koin.ktor.ext.inject
import org.koin.ktor.plugin.Koin
import org.slf4j.MDC
import kotlin.test.Test
import kotlin.test.assertEquals

class UnleashContextProviderTest : TestApplication() {
    @Test
    fun `Unleash context provider should have correct context`() =
        testApplication {
            install(Koin) {
                modules(
                    AppModule.appModule,
                    mockModule,
                )
            }
            val security =
                Security(
                    configuration.azuread,
                )
            install(Authentication) {
                security.setupMock(this, "Z999999")
            }

            routing {
                authenticate(*security.authproviders) {
                    intercept(ApplicationCallPipeline.Call) {
                        MDC.put(MDCConstants.MDC_USER_ID, call.getIdent())
                    }

                    route("/unleash") {
                        val unleashContext: UnleashContextProvider by inject()

                        get {
                            call.respondText(unleashContext.context.userId.get())
                        }
                    }
                }
            }

            startApplication()

            client.get("/unleash").apply {
                assertEquals(this.bodyAsText(), "Z999999")
            }
        }
}
