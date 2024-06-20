package no.nav.sbl.service

import io.mockk.Called
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.verify
import no.nav.sbl.config.ApplicationCluster


import no.nav.sbl.consumers.modiacontextholder.ModiaContextHolderClient
import no.nav.sbl.db.dao.EventDAO
import no.nav.sbl.db.domain.PEvent
import no.nav.sbl.redis.RedisPublisher
import no.nav.sbl.rest.domain.RSAktivBruker
import no.nav.sbl.rest.domain.RSAktivEnhet
import no.nav.sbl.rest.domain.RSContext
import no.nav.sbl.rest.domain.RSNyContext
import no.nav.sbl.service.unleash.ToggleableFeature
import no.nav.sbl.service.unleash.ToggleableFeatureService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

/**
 * Tester oppførselene til [ContextService] når applikasjonen kjører i GCP
 * og Unleash er aktivert.
 */
class ContextServiceGcpTest {
    private val toggleableFeatureService =
        mockk<ToggleableFeatureService> {
            every { isEnabled(any<ToggleableFeature>()) } returns true
        }
    private val contextHolderClient = mockk<ModiaContextHolderClient>()
    private val redisPublisher = mockk<RedisPublisher>(relaxed = true)
    private val eventDAO = mockk<EventDAO>()

    private val contextService =
        ContextService(
            eventDAO,
            redisPublisher,
            contextHolderClient,
            toggleableFeatureService,
        )

    private val veilederIdent = "veilederIdent"
    private val enhetContext = RSContext(aktivEnhet = "enhet")
    private val nyAktivEnhetEvent = PEvent(eventType = "NY_AKTIV_ENHET", verdi = "enhet")
    private val nyAktivBrukerEvent = PEvent(eventType = "NY_AKTIV_BRUKER", verdi = "bruker")

    companion object {
        @JvmStatic
        @BeforeAll
        fun beforeAll() {
            mockkObject(ApplicationCluster)
            every { ApplicationCluster.isFss() } returns false
            every { ApplicationCluster.isGcp() } returns true
            mockkObject(ContextService.Companion)
            every { ContextService.erFortsattAktuell(any()) } returns true
        }
    }

    @Test
    fun `hent veileders context burde hente context`() {
        every { eventDAO.sistAktiveBrukerEvent(any()) } returns nyAktivBrukerEvent
        every { eventDAO.sistAktiveEnhetEvent(any()) } returns nyAktivEnhetEvent

        val result = contextService.hentVeiledersContext(veilederIdent)

        verify { contextHolderClient wasNot Called }
        assertEquals(RSContext(aktivBruker = "bruker", aktivEnhet = "enhet"), result)
    }

    @Test
    fun `oppdater veileders context burde lagre til egen database og publisere til redis`() {
        every { eventDAO.save(any()) } returns 1L

        contextService.oppdaterVeiledersContext(RSNyContext("verdi", "NY_AKTIV_ENHET"), veilederIdent)

        verify { contextHolderClient wasNot Called }
        verify { eventDAO.save(any()) }
        verify { redisPublisher.publishMessage(any()) }
    }

    @Test
    fun `hent aktiv bruker burde hente aktiv bruker`() {
        every { eventDAO.sistAktiveBrukerEvent(any()) } returns nyAktivEnhetEvent

        val result = contextService.hentAktivBruker(veilederIdent)

        verify { contextHolderClient wasNot Called }
        assertEquals(enhetContext, result)
    }

    @Test
    fun `hent aktiv bruker v2 burde hente aktiv bruker v2`() {
        every { eventDAO.sistAktiveBrukerEvent(any()) } returns nyAktivBrukerEvent

        val result = contextService.hentAktivBrukerV2(veilederIdent)

        verify { contextHolderClient wasNot Called }
        assertEquals(RSAktivBruker("bruker"), result)
    }

    @Test
    fun `hent aktiv enhet burde hente aktiv enhet`() {
        every { eventDAO.sistAktiveEnhetEvent(any()) } returns nyAktivEnhetEvent

        val result = contextService.hentAktivEnhet(veilederIdent)

        verify { contextHolderClient wasNot Called }
        assertEquals(enhetContext, result)
    }

    @Test
    fun `hent aktiv enhet v2 burde hente aktiv enhet v2`() {
        every { eventDAO.sistAktiveEnhetEvent(any()) } returns nyAktivEnhetEvent

        val result = contextService.hentAktivEnhetV2(veilederIdent)

        verify { contextHolderClient wasNot Called }
        assertEquals(RSAktivEnhet("enhet"), result)
    }

    @Test
    fun `nullstill context burde slette fra egen database`() {
        every { eventDAO.slettAllEventer(any()) } returns Unit

        contextService.nullstillContext(veilederIdent)

        verify { contextHolderClient wasNot Called }
        verify { eventDAO.slettAllEventer(any()) }
    }

    @Test
    fun `nullstill aktiv bruker burde slette fra egen database`() {
        every { eventDAO.slettAlleAvEventTypeForVeileder(any(), any()) } returns Unit

        contextService.nullstillAktivBruker(veilederIdent)

        verify { contextHolderClient wasNot Called }
        verify { eventDAO.slettAlleAvEventTypeForVeileder(any(), any()) }
    }
}
