package no.nav.modiacontextholder.service

import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import no.nav.common.client.axsys.AxsysClient
import no.nav.common.client.axsys.AxsysEnhet
import no.nav.common.types.identer.EnhetId
import no.nav.modiacontextholder.rest.model.DecoratorDomain
import no.nav.modiacontextholder.utils.DistributedCache
import org.assertj.core.api.Assertions
import org.koin.test.KoinTest
import kotlin.test.Test
import kotlin.test.assertNotNull

class EnheterServiceTest : KoinTest {
    val enhetCache: EnheterCache = mockk()
    val client: AxsysClient = mockk()
    val mockCache: DistributedCache<String, List<DecoratorDomain.Enhet>?> = mockk()

    var service = EnheterService(client, enhetCache, mockCache)

    @Test
    @Throws(Exception::class)
    fun filterer_ut_inaktive_enheter() {
        every { client.hentTilganger(any()) } returns
            listOf(
                AxsysEnhet().setEnhetId(EnhetId("0001")),
                AxsysEnhet().setEnhetId(EnhetId("0002")),
                AxsysEnhet().setEnhetId(EnhetId("0003")),
            )
        every { enhetCache.get() } returns
            mapOf(
                "0002" to DecoratorDomain.Enhet("0002", "0002"),
            )
        coEvery { mockCache.get(any(), any()) } answers {
            secondArg<(String) -> List<DecoratorDomain.Enhet>>().invoke("ident")
        }

        val enheter = runBlocking { service.hentEnheter("ident") }.getOrNull()
        assertNotNull(enheter)
        Assertions.assertThat(enheter).hasSize(1)
        Assertions.assertThat(enheter[0].enhetId).isEqualTo("0002")
    }
}
