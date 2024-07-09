package no.nav.sbl.service

import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import no.nav.sbl.config.ApplicationCluster
import no.nav.sbl.consumers.modiacontextholder.ModiaContextHolderClient
import no.nav.sbl.redis.VeilederContextDatabase
import no.nav.sbl.domain.ContextEventType.NY_AKTIV_BRUKER
import no.nav.sbl.domain.ContextEvent
import no.nav.sbl.redis.RedisPublisher
import no.nav.sbl.rest.domain.RSContext
import no.nav.sbl.service.ContextService.Companion.erFortsattAktuell
import no.nav.sbl.service.unleash.ToggleableFeature
import no.nav.sbl.service.unleash.ToggleableFeatureService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class ContextServiceTest {
    private val brukerId = "bruker"

    private val veilederContextDatabase: VeilederContextDatabase = mockk()
    private val redisPublisher: RedisPublisher = mockk()
    private val contextHolderClient: ModiaContextHolderClient = mockk(relaxed = true)
    private val toggleableFeatureService: ToggleableFeatureService =
        mockk {
            every { isEnabled(any<ToggleableFeature>()) } returns false
        }
    private val contextService: ContextService =
        ContextService(
            veilederContextDatabase,
            redisPublisher,
            contextHolderClient,
            toggleableFeatureService,
        )

    companion object {
        @JvmStatic
        @BeforeAll
        fun beforeAll() {
            mockkObject(ApplicationCluster)
            every { ApplicationCluster.isFss() } returns true
            every { ApplicationCluster.isGcp() } returns false
        }
    }

    @Test
    fun ingen_aktiv_bruker_event() {
        every { contextService.hentAktivBruker(any()) } returns RSContext()
        every { veilederContextDatabase.sistAktiveBrukerEvent(any()) } returns null
        har_ikke_aktiv_bruker()
    }

    private val contextEvent =
        ContextEvent(
            veilederIdent = "veilederIdent",
            eventType = "NY_AKTIV_BRUKER",
            verdi = "bruker",
        )

    @Test
    fun eventer_fra_forrige_dag_regnes_ikke_som_aktuelle() {
        val event = contextEvent.copy(created = LocalDateTime.now().minusDays(1))
        val result = erFortsattAktuell(event)
        assertThat(result).isFalse()
    }

    @Test
    fun eventer_fra_i_dag_regnes_som_aktuelle() {
        val event = contextEvent.copy(created = LocalDateTime.now())
        val result = erFortsattAktuell(event)
        assertThat(result).isTrue()
    }

    @Test
    fun aktiv_bruker_event() {
        val now = LocalDateTime.now()
        gitt_sist_aktive_bruker_event(now)
        val rsContext = RSContext().apply { aktivBruker = brukerId }
        assertThat(contextService.hentAktivBruker("ident")).isEqualTo(rsContext)
    }

    @Test
    fun foreldet_aktiv_bruker_event() {
        gitt_sist_aktive_bruker_event(LocalDateTime.now().minusDays(2))
        har_ikke_aktiv_bruker()

        gitt_sist_aktive_bruker_event(LocalDateTime.now().minusDays(1).plusSeconds(1)) // i går men mindre enn en dag
        har_ikke_aktiv_bruker()
    }

    private fun har_ikke_aktiv_bruker() {
        assertThat(contextService.hentAktivBruker("ident")).isEqualTo(RSContext())
    }

    private fun gitt_sist_aktive_bruker_event(created: LocalDateTime) {
        val pEvent =
            contextEvent.copy(
                eventType = NY_AKTIV_BRUKER.name,
                verdi = brukerId,
                created = created,
            )
        every { veilederContextDatabase.sistAktiveBrukerEvent(any<String>()) } returns pEvent
    }
}
