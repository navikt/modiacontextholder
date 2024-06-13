package no.nav.sbl.service

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.sbl.config.ApplicationCluster
import no.nav.sbl.consumers.modiacontextholder.ModiaContextHolderClient
import no.nav.sbl.db.dao.EventDAO
import no.nav.sbl.redis.RedisPublisher
import no.nav.sbl.rest.domain.RSAktivBruker
import no.nav.sbl.rest.domain.RSAktivEnhet
import no.nav.sbl.rest.domain.RSContext
import no.nav.sbl.rest.domain.RSNyContext
import no.nav.sbl.service.unleash.Feature
import no.nav.sbl.service.unleash.UnleashService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * Tester oppførselene til [ContextService] når applikasjonen kjører i FSS
 * og Unleash er aktivert.
 */
class ContextServiceFssTest {
    private val applicationCluster = mockk<ApplicationCluster> {
        every { isFss() } returns true
        every { isGcp() } returns false
    }
    private val unleashService = mockk<UnleashService> {
        every { isEnabled(any<Feature>()) } returns true
    }
    private val contextHolderClient = mockk<ModiaContextHolderClient>()
    private val redisPublisher = mockk<RedisPublisher>(relaxed = true)
    private val eventDAO = mockk<EventDAO>()


    private val contextService = ContextService(
        eventDAO, redisPublisher, contextHolderClient, unleashService, applicationCluster
    )

    private val veilederIdent = "veilederIdent"
    private val rsContext = RSContext("bruker", "enhet")
    private val nyContext = RSNyContext("verdi", "NY_AKTIV_ENHET")
    private val aktivBruker = RSAktivBruker("bruker")
    private val aktivEnhet = RSAktivEnhet("enhet")

    @Test
    fun `hent veileders context burde hente context fra proxy`() {
        every { contextHolderClient.hentVeiledersContext(any()) } returns Result.success(rsContext)

        val result = contextService.hentVeiledersContext(veilederIdent)

        verify { contextHolderClient.hentVeiledersContext(any()) }
        assertEquals(rsContext, result)
    }

    @Test
    fun `oppdater veileders context burde oppdatere context i proxy, lagre til egen database og publisere til redis`() {
        every { contextHolderClient.oppdaterVeiledersContext(any(), any()) } returns Result.success(Unit)
        every { eventDAO.save(any()) } returns 1L

        contextService.oppdaterVeiledersContext(nyContext, veilederIdent)

        verify { contextHolderClient.oppdaterVeiledersContext(any(), any()) }
        verify { eventDAO.save(any()) }
        verify { redisPublisher.publishMessage(any()) }
    }

    @Test
    fun `hent aktiv bruker burde hente aktiv bruker fra proxy`() {
        every { contextHolderClient.hentAktivBruker(any()) } returns Result.success(rsContext)

        val result = contextService.hentAktivBruker(veilederIdent)

        verify { contextHolderClient.hentAktivBruker(any()) }
        assertEquals(rsContext, result)
    }

    @Test
    fun `hent aktiv bruker v2 burde hente aktiv bruker v2 fra proxy`() {
        every { contextHolderClient.hentAktivBrukerV2(any()) } returns Result.success(aktivBruker)

        val result = contextService.hentAktivBrukerV2(veilederIdent)

        verify { contextHolderClient.hentAktivBrukerV2(any()) }
        assertEquals(aktivBruker, result)
    }

    @Test
    fun `hent aktiv enhet burde hente aktiv enhet fra proxy`() {
        every { contextHolderClient.hentAktivEnhet(any()) } returns Result.success(rsContext)

        val result = contextService.hentAktivEnhet(veilederIdent)

        verify { contextHolderClient.hentAktivEnhet(any()) }
        assertEquals(rsContext, result)
    }

    @Test
    fun `hent aktiv enhet v2 burde hente aktiv enhet v2 fra proxy`() {
        every { contextHolderClient.hentAktivEnhetV2(any()) } returns Result.success(aktivEnhet)

        val result = contextService.hentAktivEnhetV2(veilederIdent)

        verify { contextHolderClient.hentAktivEnhetV2(any()) }
        assertEquals(aktivEnhet, result)
    }

    @Test
    fun `nullstill context burde nullstille context i proxy og slette fra egen database`() {
        every { contextHolderClient.nullstillContext(any()) } returns Result.success(Unit)
        every { eventDAO.slettAllEventer(any()) } returns Unit

        contextService.nullstillContext(veilederIdent)

        verify { contextHolderClient.nullstillContext(any()) }
        verify { eventDAO.slettAllEventer(any()) }
    }

    @Test
    fun `nullstill aktiv bruker burde nullstille aktiv bruker i proxy og slette fra egen database`() {
        every { contextHolderClient.nullstillAktivBruker(any()) } returns Result.success(Unit)
        every { eventDAO.slettAlleAvEventTypeForVeileder(any(), any()) } returns Unit

        contextService.nullstillAktivBruker(veilederIdent)

        verify { contextHolderClient.nullstillAktivBruker(any()) }
        verify { eventDAO.slettAlleAvEventTypeForVeileder(any(), any()) }
    }
}