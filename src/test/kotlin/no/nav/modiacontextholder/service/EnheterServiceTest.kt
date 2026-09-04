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
                AnsattRolle("0000-GA-ENHET_0002", "1234"),
                AnsattRolle("0000-GA-ENHET_0003", "0003"),
                AnsattRolle("0000-GA-ENHET_0004", "0004"),
            )
        every { enhetCache.get() } returns
            mapOf(
                "0002" to DecoratorDomain.Enhet("0002", "0002","1234"),
            )

        val enheter = runBlocking { service.hentEnheter("ident", "token") }.getOrNull()
        assertNotNull(enheter)
        Assertions.assertThat(enheter).hasSize(1)
        Assertions.assertThat(enheter[0].enhetId).isEqualTo("0002")
        Assertions.assertThat(enheter[0].gruppeId).isEqualTo("1234")
    }

    @Test
    fun filtrerer_bort_ikke_oppgavebehandlere_ved_opt_in() = testApp {
        coEvery { azureADService.fetchRoller(any(), any()) } returns
            listOf(
                AnsattRolle("0000-GA-ENHET_0001", "1"),
                AnsattRolle("0000-GA-ENHET_0002", "2"),
                AnsattRolle("0000-GA-ENHET_0003", "3"),
            )
        every { enhetCache.get() } returns
            mapOf(
                "0001" to DecoratorDomain.Enhet("0001", "Enhet 1", oppgavebehandler = true),
                "0002" to DecoratorDomain.Enhet("0002", "Enhet 2", oppgavebehandler = false),
                "0003" to DecoratorDomain.Enhet("0003", "Enhet 3", oppgavebehandler = null),
            )

        val enheter =
            runBlocking { service.hentEnheter("ident-optin", "token", kunOppgavebehandlere = true) }.getOrNull()

        assertNotNull(enheter)
        // 0003 har ukjent verdi (null) og likestilles med false
        Assertions.assertThat(enheter.map { it.enhetId }).containsExactly("0001")
    }

    @Test
    fun hentAlleEnheter_respekterer_opt_in() = testApp {
        every { enhetCache.getAll() } returns
            listOf(
                DecoratorDomain.Enhet("0001", "Enhet 1", oppgavebehandler = true),
                DecoratorDomain.Enhet("0002", "Enhet 2", oppgavebehandler = false),
                DecoratorDomain.Enhet("0003", "Enhet 3", oppgavebehandler = null),
            )

        Assertions.assertThat(service.hentAlleEnheter()).hasSize(3)
        Assertions
            .assertThat(service.hentAlleEnheter(kunOppgavebehandlere = true).map { it.enhetId })
            .containsExactly("0001")
    }
}
