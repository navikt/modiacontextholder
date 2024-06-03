package no.nav.sbl.service

import no.nav.common.client.axsys.AxsysClient
import no.nav.common.client.axsys.AxsysEnhet
import no.nav.common.types.identer.EnhetId
import no.nav.sbl.rest.domain.DecoratorDomain
import org.assertj.core.api.Assertions
import org.junit.Test
import org.mockito.ArgumentMatchers
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import java.util.*

class EnheterServiceTest {
    @Mock
    lateinit var enhetCache: EnheterCache

    @Mock
    lateinit var client: AxsysClient

    @InjectMocks
    lateinit var service: EnheterService

    @Test
    @Throws(Exception::class)
    fun filterer_ut_inaktive_enheter() {
        gitt_tilgang_til_enheter(listOf(
            AxsysEnhet().setEnhetId(EnhetId("001")),
            AxsysEnhet().setEnhetId(EnhetId("002")),
            AxsysEnhet().setEnhetId(EnhetId("003"))))
        gitt_aktive_enheter(
            listOf(
                DecoratorDomain.Enhet("0002", "0002")
            )
        )
        val enheter = service.hentEnheter("").get()
        Assertions.assertThat(enheter).hasSize(1)
        Assertions.assertThat(enheter[0].enhetId).isEqualTo("0002")
    }

    private fun gitt_tilgang_til_enheter(enheter: List<AxsysEnhet>) {
        Mockito.`when`(client.hentTilganger(ArgumentMatchers.any())).thenReturn(enheter)
    }

    private fun gitt_aktive_enheter(data: List<DecoratorDomain.Enhet>) {
        val cache = data.map { it.enhetId to it }.toMap()
        Mockito.`when`(enhetCache.get()).thenReturn(cache)
    }
}