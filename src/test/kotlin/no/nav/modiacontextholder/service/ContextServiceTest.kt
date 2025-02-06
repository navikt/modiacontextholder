package no.nav.modiacontextholder.service

import io.mockk.every
import io.mockk.mockk
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import no.nav.modiacontextholder.domain.VeilederContext
import no.nav.modiacontextholder.domain.VeilederContextType.NY_AKTIV_BRUKER
import no.nav.modiacontextholder.valkey.ValkeyPublisher
import no.nav.modiacontextholder.valkey.VeilederContextDatabase
import no.nav.modiacontextholder.rest.model.RSContext
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ContextServiceTest {
    private val brukerId = "bruker"

    private val veilederContextDatabase: VeilederContextDatabase = mockk()
    private val redisPublisher: ValkeyPublisher = mockk()
    private val contextService: ContextService =
        ContextService(
            veilederContextDatabase,
            redisPublisher,
        )

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
        val now = Clock.System.now().toLocalDateTime(TimeZone.UTC)
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
