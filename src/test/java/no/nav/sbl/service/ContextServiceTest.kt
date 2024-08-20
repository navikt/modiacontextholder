package no.nav.sbl.service

import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import no.nav.sbl.config.ApplicationCluster
import no.nav.sbl.consumers.modiacontextholder.ModiaContextHolderClient
import no.nav.sbl.domain.VeilederContext
import no.nav.sbl.domain.VeilederContextType.NY_AKTIV_BRUKER
import no.nav.sbl.redis.RedisPublisher
import no.nav.sbl.redis.VeilederContextDatabase
import no.nav.sbl.rest.model.RSContext
import no.nav.sbl.service.unleash.ToggleableFeature
import no.nav.sbl.service.unleash.ToggleableFeatureService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import java.time.LocalDate
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
            listOf(redisPublisher),
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

        @JvmStatic
        fun erFortsattAktuell(veilederContext: VeilederContext): Boolean = LocalDate.now().isEqual(veilederContext.created?.toLocalDate())
    }

    @Test
    fun ingen_aktiv_bruker_event() {
        every { contextService.hentAktivBruker(any()) } returns RSContext()
        every { veilederContextDatabase.sistAktiveBrukerEvent(any()) } returns null
        har_ikke_aktiv_bruker()
    }

    private val veilederContext =
        VeilederContext(
            veilederIdent = "veilederIdent",
            contextType = NY_AKTIV_BRUKER,
            verdi = "bruker",
        )

    @Test
    fun aktiv_bruker_event() {
        val now = LocalDateTime.now()
        gitt_sist_aktive_bruker_event(now)
        val rsContext = RSContext().apply { aktivBruker = brukerId }
        assertThat(contextService.hentAktivBruker("ident")).isEqualTo(rsContext)
    }

    private fun har_ikke_aktiv_bruker() {
        assertThat(contextService.hentAktivBruker("ident")).isEqualTo(RSContext())
    }

    private fun gitt_sist_aktive_bruker_event(created: LocalDateTime) {
        val pEvent =
            veilederContext.copy(
                contextType = NY_AKTIV_BRUKER,
                verdi = brukerId,
                created = created,
            )
        every { veilederContextDatabase.sistAktiveBrukerEvent(any<String>()) } returns pEvent
    }
}
