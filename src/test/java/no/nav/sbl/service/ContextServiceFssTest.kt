package no.nav.sbl.service

import io.mockk.Called
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.verify
import no.nav.sbl.config.ApplicationCluster
import no.nav.sbl.consumers.modiacontextholder.ModiaContextHolderClient
import no.nav.sbl.redis.RedisPublisher
import no.nav.sbl.redis.VeilederContextDatabase
import no.nav.sbl.rest.model.RSAktivBruker
import no.nav.sbl.rest.model.RSAktivEnhet
import no.nav.sbl.rest.model.RSContext
import no.nav.sbl.rest.model.RSNyContext
import no.nav.sbl.service.unleash.ToggleableFeature
import no.nav.sbl.service.unleash.ToggleableFeatureService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

/**
 * Tester oppførselene til [ContextService] når applikasjonen kjører i FSS
 * og Unleash er aktivert.
 */
class ContextServiceFssTest {
    private val toggleableFeatureService =
        mockk<ToggleableFeatureService> {
            every { isEnabled(any<ToggleableFeature>()) } returns true
        }
    private val contextHolderClient = mockk<ModiaContextHolderClient>()
    private val redisPublisher = mockk<RedisPublisher>(relaxed = true)
    private val veilederContextDatabase = mockk<VeilederContextDatabase>()

    private val contextService =
        ContextService(
            veilederContextDatabase,
            redisPublisher,
            contextHolderClient,
            toggleableFeatureService,
        )

    private val veilederIdent = "veilederIdent"
    private val rsContext = RSContext("bruker", "enhet")
    private val nyContext = RSNyContext("verdi", "NY_AKTIV_ENHET")
    private val aktivBruker = RSAktivBruker("bruker")
    private val aktivEnhet = RSAktivEnhet("enhet")

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
    fun `hent veileders context burde hente context fra proxy`() {
        every { contextHolderClient.hentVeiledersContext(any()) } returns Result.success(rsContext)

        val result = contextService.hentVeiledersContext(veilederIdent)

        verify { contextHolderClient.hentVeiledersContext(any()) }
        assertEquals(rsContext, result)
    }

    @Test
    fun `oppdater veileders context burde oppdatere context i proxy og publisere til redis`() {
        every { contextHolderClient.oppdaterVeiledersContext(any(), any()) } returns Result.success(rsContext)

        contextService.oppdaterVeiledersContext(nyContext, veilederIdent)

        verify { contextHolderClient.oppdaterVeiledersContext(any(), any()) }
        verify { veilederContextDatabase wasNot Called }
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
    fun `nullstill context burde nullstille context i proxy`() {
        every { contextHolderClient.nullstillBrukerContext(any()) } returns Result.success(Unit)

        contextService.nullstillContext(veilederIdent)

        verify { contextHolderClient.nullstillBrukerContext(any()) }
        verify { veilederContextDatabase wasNot Called }
    }

    @Test
    fun `nullstill aktiv bruker burde nullstille aktiv bruker i proxy`() {
        every { contextHolderClient.nullstillAktivBruker(any()) } returns Result.success(Unit)

        contextService.nullstillAktivBruker(veilederIdent)

        verify { contextHolderClient.nullstillAktivBruker(any()) }
        verify { veilederContextDatabase wasNot Called }
    }
}
