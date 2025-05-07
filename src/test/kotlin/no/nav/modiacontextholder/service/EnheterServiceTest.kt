package no.nav.modiacontextholder.service

import io.mockk.coEvery
import io.mockk.every
import kotlinx.coroutines.runBlocking
import no.nav.modiacontextholder.rest.TestApplication
import no.nav.modiacontextholder.rest.model.DecoratorDomain
import org.assertj.core.api.Assertions
import org.koin.test.get
import org.koin.test.mock.declareMock
import kotlin.test.Test
import kotlin.test.assertNotNull

class EnheterServiceTest : TestApplication() {
    val enhetCache: EnheterCache by lazy { declareMock() }
    val azureADService: AzureADService by lazy { declareMock() }
    val service: EnheterService by lazy { get<EnheterService>() }

    @Test
    fun filterer_ut_inaktive_enheter() = testApp {
        coEvery { azureADService.fetchRoller(any(), any()) } returns
            listOf(
                AnsattRolle("0000-GA-ENHET_0001", "0001"),
                AnsattRolle("0000-GA-ENHET_0002", "0002"),
                AnsattRolle("0000-GA-ENHET_0003", "0003"),
                AnsattRolle("0000-GA-ENHET_0004", "0004"),
            )
        every { enhetCache.get() } returns
            mapOf(
                "0002" to DecoratorDomain.Enhet("0002", "0002"),
            )

        val enheter = runBlocking { service.hentEnheter("ident", "token") }.getOrNull()
        assertNotNull(enheter)
        Assertions.assertThat(enheter).hasSize(1)
        Assertions.assertThat(enheter[0].enhetId).isEqualTo("0002")
    }
}
