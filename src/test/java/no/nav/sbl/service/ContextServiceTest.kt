package no.nav.sbl.service

import io.mockk.every
import io.mockk.mockk
import no.nav.sbl.config.ApplicationCluster
import no.nav.sbl.consumers.modiacontextholder.ModiaContextHolderClient
import no.nav.sbl.db.dao.EventDAO
import no.nav.sbl.db.domain.EventType.NY_AKTIV_BRUKER
import no.nav.sbl.db.domain.PEvent
import no.nav.sbl.redis.RedisPublisher
import no.nav.sbl.rest.domain.RSContext
import no.nav.sbl.service.ContextService.Companion.erFortsattAktuell
import no.nav.sbl.service.unleash.Feature
import no.nav.sbl.service.unleash.UnleashService
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.time.LocalDateTime
import java.util.Optional

class ContextServiceTest {

    private val brukerId = "bruker"

    private val eventDAO: EventDAO = mockk()
    private val redisPublisher: RedisPublisher = mockk()
    private val contextHolderClient: ModiaContextHolderClient = mockk(relaxed = true)
    private val unleashService: UnleashService = mockk {
        every { isEnabled(any<Feature>()) } returns false
    }
    private val applicationCluster: ApplicationCluster = ApplicationCluster.PROD_FSS
    private val contextService: ContextService = ContextService(
        eventDAO, redisPublisher,
        contextHolderClient, unleashService, applicationCluster
    )

    @Test
    fun ingen_aktiv_bruker_event() {
        every { contextService.hentAktivBruker(any()) } returns RSContext()
        every { eventDAO.sistAktiveBrukerEvent(any()) } returns Optional.empty()
        har_ikke_aktiv_bruker()
    }

    @Test
    fun eventer_fra_forrige_dag_regnes_ikke_som_aktuelle() {
        val event = PEvent().apply { created = LocalDateTime.now().minusDays(1) }
        val result = erFortsattAktuell(event)
        assertThat(result).isFalse()
    }

    @Test
    fun eventer_fra_i_dag_regnes_som_aktuelle() {
        val event = PEvent().apply { created = LocalDateTime.now() }
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
        val pEvent = PEvent().apply {
            eventType = NY_AKTIV_BRUKER.name
            verdi = brukerId
            this.created = created
        }
        every { eventDAO.sistAktiveBrukerEvent(any<String>()) } returns Optional.of(pEvent)
    }
}
