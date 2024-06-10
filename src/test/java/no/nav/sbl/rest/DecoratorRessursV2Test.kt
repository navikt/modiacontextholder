package no.nav.sbl.rest

import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.vavr.control.Try
import no.nav.common.types.identer.AzureObjectId
import no.nav.sbl.azure.AnsattRolle
import no.nav.sbl.azure.AzureADService
import no.nav.sbl.rest.domain.DecoratorDomain
import no.nav.sbl.service.AuthContextService
import no.nav.sbl.service.EnheterService
import no.nav.sbl.service.VeilederService
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.web.server.ResponseStatusException
import java.util.*

@ExtendWith(MockKExtension::class)
class DecoratorRessursV2Test {
    private val ident = "Z999999"
    private val groupName = "0000-GA-GOSYS_REGIONAL"
    private val adminGroupName = "0000-GA-Modia_Admin"
    private val azureObjectId = AzureObjectId("d2987104-63b2-4110-83ac-20ff6afe24a2")

    private val azureADService: AzureADService = mockk {
        every { fetchRoller(any(), any()) } returns listOf(AnsattRolle(groupName, azureObjectId))
    }
    private val enheterService: EnheterService = mockk()
    private val veilederService: VeilederService = mockk()
    private val authContextService: AuthContextService = mockk(relaxed = true)
    private val rest: DecoratorRessursV2 =
        DecoratorRessursV2(azureADService, enheterService, veilederService, mockk(), authContextService)

    @Test
    fun ingen_saksbehandlerident() {
        gitt_saksbehandler_i_ad()
        shouldWorkRegardlessOfFeatureToggle {
            assertThatThrownBy { rest.hentSaksbehandlerInfoOgEnheter() }
                .isInstanceOf(ResponseStatusException::class.java)
                .hasMessage("500 INTERNAL_SERVER_ERROR \"Fant ingen subjecthandler\"")
        }
    }

    @Test
    fun feil_ved_henting_av_enheter() {
        gitt_logget_inn()
        gitt_saksbehandler_i_ad()
        every { enheterService.hentEnheter(ident) } returns Try.failure(IllegalStateException("Noe gikk feil"))

        shouldWorkRegardlessOfFeatureToggle {
            assertThatThrownBy { rest.hentSaksbehandlerInfoOgEnheter() }
                .hasRootCauseInstanceOf(IllegalStateException::class.java)
                .isInstanceOf(ResponseStatusException::class.java)
                .hasMessageStartingWith("500 INTERNAL_SERVER_ERROR \"Kunne ikke hente data\"")
        }
    }

    @Test
    fun returnerer_saksbehandlernavn() {
        gitt_logget_inn()
        gitt_saksbehandler_i_ad()
        gitt_tilgang_til_enheter(emptyList())

        shouldWorkRegardlessOfFeatureToggle {
            val decoratorConfig = rest.hentSaksbehandlerInfoOgEnheter()

            assertThat(decoratorConfig.ident).isEqualTo(ident)
            assertThat(decoratorConfig.fornavn).isEqualTo("Fornavn")
            assertThat(decoratorConfig.etternavn).isEqualTo("Etternavn")
        }

        shouldWorkRegardlessOfFeatureToggle {
            val decoratorConfig = rest.hentSaksbehandlerInfoOgEnheter()

            assertThat(decoratorConfig.ident).isEqualTo(ident)
            assertThat(decoratorConfig.fornavn).isEqualTo("Fornavn")
            assertThat(decoratorConfig.etternavn).isEqualTo("Etternavn")
        }
    }

    @Test
    fun bare_aktive_enheter() {
        gitt_logget_inn()
        gitt_saksbehandler_i_ad()
        gitt_tilgang_til_enheter(
            listOf(
                enhet("0001", "Test 1"),
                enhet("0002", "Test 2"),
                enhet("0003", "Test 3")
            )
        )

        shouldWorkRegardlessOfFeatureToggle {
            val decoratorConfig = rest.hentSaksbehandlerInfoOgEnheter()
            assertThat(decoratorConfig.enheter).hasSize(3)
        }
    }

    @Test
    fun alle_enheter_om_saksbehandler_har_modia_admin() {
        gitt_logget_inn()
        every { azureADService.fetchRoller(any(), any()) } returns listOf(AnsattRolle(adminGroupName, azureObjectId))
        gitt_saksbehandler_i_ad()
        gitt_tilgang_til_enheter(
            listOf(
                enhet("0001", "Test 1")
            )
        )

        shouldWorkRegardlessOfFeatureToggle {
            val decoratorConfig = rest.hentSaksbehandlerInfoOgEnheter()
            assertThat(decoratorConfig.enheter).hasSize(5)
        }
    }

    private fun gitt_tilgang_til_enheter(data: List<DecoratorDomain.Enhet>) {
        every { enheterService.hentEnheter(ident) } returns Try.of { data }
        every { enheterService.hentAlleEnheter() } returns listOf(
            enhet("0001", "Test 1"),
            enhet("0002", "Test 2"),
            enhet("0003", "Test 3"),
            enhet("0004", "Test 4"),
            enhet("0005", "Test 5")
        )
    }

    private fun gitt_saksbehandler_i_ad() {
        every { veilederService.hentVeilederNavn(any()) } returns DecoratorDomain.Saksbehandler(
            ident,
            "Fornavn",
            "Etternavn"
        )
    }

    private fun gitt_logget_inn() {
        every { authContextService.ident } returns Optional.of(ident)
    }

    private fun enhet(id: String, navn: String): DecoratorDomain.Enhet {
        return DecoratorDomain.Enhet(id, navn)
    }

    private fun shouldWorkRegardlessOfFeatureToggle(executable: () -> Unit) {
        executable.invoke()
    }
}
