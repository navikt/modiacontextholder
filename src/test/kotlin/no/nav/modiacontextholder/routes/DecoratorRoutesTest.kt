package no.nav.modiacontextholder.routes

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.mockk.every
import io.mockk.mockk
import io.vavr.control.Try
import no.nav.common.types.identer.AzureObjectId
import no.nav.modiacontextholder.rest.TestApplication
import no.nav.modiacontextholder.rest.model.DecoratorDomain
import no.nav.modiacontextholder.service.AnsattRolle
import no.nav.modiacontextholder.service.AzureADService
import no.nav.modiacontextholder.service.EnheterService
import no.nav.modiacontextholder.service.VeilederService
import org.junit.jupiter.api.Assertions.assertEquals
import org.koin.test.mock.declareMock
import java.util.*
import kotlin.test.Test

class DecoratorRoutesTest : TestApplication() {
    private val ident = "Z999999"
    private val adminGroupName = "0000-GA-Modia_Admin"
    private val regionalGroupName = "0000-GA-GOSYS_REGIONAL"
    private val azureObjectId = AzureObjectId("d2987104-63b2-4110-83ac-20ff6afe24a2")

    private val azureADService: AzureADService =
        mockk {
            every { fetchRoller(any(), any()) } returns listOf(AnsattRolle(regionalGroupName, azureObjectId))
        }
    private val enheterService: EnheterService = mockk()
    private val veilederService: VeilederService = mockk()

    @Test
    fun `ingen saksbehandlerident`() {
        gittSaksbehandlerIAd()
        testApp {
            client.get("/api/v2/decorator").apply {
                assertEquals(HttpStatusCode.Forbidden, this.status)
                assert(this.bodyAsText().contains("Missing or invalid authorization header"))
            }
        }
    }

    @Test
    fun feil_ved_henting_av_enheter() {
        gittSaksbehandlerIAd()

        testApp {
            every { enheterService.hentEnheter(ident) } returns Try.failure(IllegalStateException("Noe gikk feil"))
            declareMock<EnheterService> { enheterService }

            client.get("/api/v2/decorator") { gittLoggetInn() }.apply {
                assertEquals(this.status, HttpStatusCode.InternalServerError)
                assert(this.bodyAsText().contains("Kunne ikke hente data"))
            }
        }
    }

    @Test
    fun testGetV2Decorator() =
        testApp {
            client.get("/v2/decorator").apply {
                TODO("Please write your test here")
            }
        }

    @Test
    fun testPostV2DecoratorAktorHentfnr() =
        testApp {
            client.post("/v2/decorator/aktor/hent-fnr").apply {
                TODO("Please write your test here")
            }
        }

    private fun gittTilgangTilEnheter(data: List<DecoratorDomain.Enhet>) {
        every { enheterService.hentEnheter(ident) } returns Try.of { data }
        every { enheterService.hentAlleEnheter() } returns
            listOf(
                enhet("0001", "Test 1"),
                enhet("0002", "Test 2"),
                enhet("0003", "Test 3"),
                enhet("0004", "Test 4"),
                enhet("0005", "Test 5"),
            )
    }

    private fun gittSaksbehandlerIAd() {
        every { veilederService.hentVeilederNavn(any()) } returns
            DecoratorDomain.Saksbehandler(
                ident,
                "Fornavn",
                "Etternavn",
            )
    }

    private fun gittLoggetInn() {
        headers {
            append("Authorization", "Bearer token")
        }
    }

    private fun enhet(
        id: String,
        navn: String,
    ): DecoratorDomain.Enhet = DecoratorDomain.Enhet(id, navn)
}
