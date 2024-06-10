package no.nav.sbl.service

import io.mockk.every
import io.mockk.mockk
import no.nav.common.client.axsys.AxsysClient
import no.nav.common.client.axsys.AxsysEnhet
import no.nav.common.types.identer.EnhetId
import no.nav.sbl.rest.domain.DecoratorDomain
import org.assertj.core.api.Assertions
import org.junit.Test

class EnheterServiceTest {
    val enhetCache: EnheterCache = mockk()
    val client: AxsysClient = mockk()

    val service: EnheterService = EnheterService(client, enhetCache)

    @Test
    @Throws(Exception::class)
    fun filterer_ut_inaktive_enheter() {
        every { client.hentTilganger(any()) } returns listOf(
            AxsysEnhet().setEnhetId(EnhetId("0001")),
            AxsysEnhet().setEnhetId(EnhetId("0002")),
            AxsysEnhet().setEnhetId(EnhetId("0003"))
        )
        every { enhetCache.get() } returns mapOf(
            "0002" to DecoratorDomain.Enhet("0002", "0002"),
        )
        val enheter = service.hentEnheter("ident").get()
        Assertions.assertThat(enheter).hasSize(1)
        Assertions.assertThat(enheter[0].enhetId).isEqualTo("0002")
    }
}