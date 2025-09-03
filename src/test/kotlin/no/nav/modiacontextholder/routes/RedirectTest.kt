package no.nav.modiacontextholder.routes

import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import no.nav.modiacontextholder.config.Configuration
import no.nav.modiacontextholder.domain.VeilederContextType
import no.nav.modiacontextholder.rest.TestApplication
import no.nav.modiacontextholder.rest.model.RSNyContext
import no.nav.modiacontextholder.service.ContextService
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.koin.test.inject
import kotlin.test.Test
import kotlin.test.assertEquals

class RedirectTest : TestApplication() {
    @Test
    fun `get redirect with user context from aaregister`() {
        val mockAAregisterServer = MockWebServer()
        mockAAregisterServer.enqueue(MockResponse().setBody("redirectUrlFromAAregister"))
        mockAAregisterServer.start()

        configuration =
            Configuration(
                redisUri = configuration.redisUri,
                aaRegisteretPublicUrl = configuration.aaRegisteretPublicUrl,
                salesforceBaseUrl = configuration.salesforceBaseUrl,
                aaRegisteretBaseUrl = mockAAregisterServer.url("/").toString(),
            )
        testApp {
            val contextService: ContextService by inject()
            contextService.oppdaterVeiledersContext(
                RSNyContext("12345678910", VeilederContextType.NY_AKTIV_BRUKER),
                "Z999999")

            it.getAuth("/redirect/aaregisteret").apply {
                assertEquals(HttpStatusCode.Found, this.status)
                assertEquals("redirectUrlFromAAregister", this.headers[HttpHeaders.Location])
            }
        }
    }

    @Test
    fun `get redirect without user context from aaregister`() =
        testApp {
            val contextService: ContextService by inject()
            contextService.nullstillContext("Z999999")

            it.get("/redirect/aaregisteret").apply {
                assertEquals(HttpStatusCode.Found, this.status)
                assertEquals(configuration.aaRegisteretPublicUrl, this.headers[HttpHeaders.Location])
            }
        }

    @Test
    fun testGetRedirectSalesforce() =
        testApp {
            it.get("/redirect/salesforce").apply {
                assertEquals(HttpStatusCode.Found, this.status)
                assertEquals(configuration.salesforceBaseUrl, this.headers[HttpHeaders.Location])
            }
        }
}
