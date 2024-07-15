package no.nav.sbl.rest

import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.vavr.control.Try
import no.nav.common.types.identer.AzureObjectId
import no.nav.sbl.azure.AnsattRolle
import no.nav.sbl.azure.AzureADService
import no.nav.sbl.rest.model.DecoratorDomain
import no.nav.sbl.service.AuthContextService
import no.nav.sbl.service.EnheterService
import no.nav.sbl.service.VeilederService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.web.server.ResponseStatusException
import java.util.Optional

@ExtendWith(MockKExtension::class)
class DecoratorRessursTest {
    private val ident = "Z999999"
    private val adminGroupName = "0000-GA-Modia_Admin"
    private val regionalGroupName = "0000-GA-GOSYS_REGIONAL"
    private val azureObjectId = AzureObjectId("d2987104-63b2-4110-83ac-20ff6afe24a2")

    private val azureADService: AzureADService = mockk {
        every { fetchRoller(any(), any()) } returns listOf(AnsattRolle(regionalGroupName, azureObjectId))
    }
    private val enheterService: EnheterService = mockk()
    private val veilederService: VeilederService = mockk()
    private val authContextService: AuthContextService = mockk(relaxed = true)

    private val rest = DecoratorRessurs(azureADService, enheterService, veilederService, mockk(), authContextService)

    @Test
    fun `ingen saksbehandlerident`() {
        gittSaksbehandlerIAd()
        assertThrows(ResponseStatusException::class.java) {
            rest.hentSaksbehandlerInfoOgEnheter()
        }.apply {
            assertEquals("500 INTERNAL_SERVER_ERROR \"Fant ingen subjecthandler\"", message)
        }
    }

    @Test
    fun `feil ved henting av enheter`() {
        gittLoggetInn()
        gittSaksbehandlerIAd()
        every { enheterService.hentEnheter(ident) } returns Try.failure(IllegalStateException("Noe gikk feil"))
        every { enheterService.hentAlleEnheter() } returns listOf(
            enhet("0001", "Test 1"),
            enhet("0002", "Test 2"),
            enhet("0003", "Test 3"),
            enhet("0004", "Test 4"),
            enhet("0005", "Test 5")
        )

        assertThrows(ResponseStatusException::class.java) {
            rest.hentSaksbehandlerInfoOgEnheter()
        }.apply {
            assertEquals("500 INTERNAL_SERVER_ERROR \"Kunne ikke hente data\"", message)
        }
    }

    @Test
    fun `returnerer saksbehandlernavn`() {
        gittLoggetInn()
        gittSaksbehandlerIAd()
        gittTilgangTilEnheter(emptyList())

        val decoratorConfig = rest.hentSaksbehandlerInfoOgEnheter()

        assertEquals(ident, decoratorConfig.ident)
        assertEquals("Fornavn", decoratorConfig.fornavn)
        assertEquals("Etternavn", decoratorConfig.etternavn)
    }

    @Test
    fun `bare aktive enheter`() {
        gittLoggetInn()
        gittSaksbehandlerIAd()
        gittTilgangTilEnheter(
            listOf(
                enhet("0001", "Test 1"),
                enhet("0002", "Test 2"),
                enhet("0003", "Test 3")
            )
        )

        val decoratorConfig = rest.hentSaksbehandlerInfoOgEnheter()
        assertEquals(3, decoratorConfig.enheter.size)
    }

    @Test
    fun `alle enheter om saksbehandler har modia admin`() {
        gittLoggetInn()
        every { azureADService.fetchRoller(any(), any()) } returns listOf(AnsattRolle(adminGroupName, azureObjectId))
        gittSaksbehandlerIAd()
        gittTilgangTilEnheter(
            listOf(
                enhet("0001", "Test 1")
            )
        )

        val decoratorConfig = rest.hentSaksbehandlerInfoOgEnheter()
        assertEquals(5, decoratorConfig.enheter.size)
    }

    private fun gittTilgangTilEnheter(data: List<DecoratorDomain.Enhet>) {
        every { enheterService.hentEnheter(ident) } returns Try.of { data }
        every { enheterService.hentAlleEnheter() } returns listOf(
            enhet("0001", "Test 1"),
            enhet("0002", "Test 2"),
            enhet("0003", "Test 3"),
            enhet("0004", "Test 4"),
            enhet("0005", "Test 5")
        )
    }

    private fun gittSaksbehandlerIAd() {
        every { veilederService.hentVeilederNavn(any()) } returns DecoratorDomain.Saksbehandler(
            ident,
            "Fornavn",
            "Etternavn"
        )
    }

    private fun gittLoggetInn() {
        every { authContextService.requireIdToken() } returns "token"
        every { authContextService.ident } returns Optional.of(ident)
    }

    private fun enhet(id: String, navn: String): DecoratorDomain.Enhet {
        return DecoratorDomain.Enhet(id, navn)
    }
}