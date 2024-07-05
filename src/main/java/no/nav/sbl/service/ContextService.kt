package no.nav.sbl.service

import no.nav.common.json.JsonUtils
import no.nav.sbl.config.ApplicationCluster
import no.nav.sbl.consumers.modiacontextholder.ModiaContextHolderClient
import no.nav.sbl.db.VeilederContextDatabase
import no.nav.sbl.db.domain.EventType
import no.nav.sbl.db.domain.PEvent
import no.nav.sbl.redis.RedisPublisher
import no.nav.sbl.rest.domain.RSAktivBruker
import no.nav.sbl.rest.domain.RSAktivEnhet
import no.nav.sbl.rest.domain.RSContext
import no.nav.sbl.rest.domain.RSEvent
import no.nav.sbl.rest.domain.RSNyContext
import no.nav.sbl.service.unleash.ToggleableFeatureService
import no.nav.sbl.service.unleash.ToggleableFeatures
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class ContextService(
    private val veilederContextDatabase: VeilederContextDatabase,
    private val redisPublisher: RedisPublisher,
    private val contextHolderClient: ModiaContextHolderClient,
    private val toggleableFeatureService: ToggleableFeatureService,
) {
    private val log = LoggerFactory.getLogger(ContextService::class.java)

    companion object {
        @JvmStatic
        fun erFortsattAktuell(pEvent: PEvent): Boolean = LocalDate.now().isEqual(pEvent.created?.toLocalDate())
    }

    fun hentVeiledersContext(veilederIdent: String): RSContext =
        if (burdeSynceContextMedGcp()) {
            contextHolderClient
                .hentVeiledersContext(veilederIdent)
                .getOrThrow()
        } else {
            RSContext(
                hentAktivBruker(veilederIdent).aktivBruker,
                hentAktivEnhet(veilederIdent).aktivEnhet,
            )
        }

    fun oppdaterVeiledersContext(
        nyContext: RSNyContext,
        veilederIdent: String,
    ) {
        val event =
            PEvent(
                verdi = nyContext.verdi,
                eventType = nyContext.eventType,
                veilederIdent = veilederIdent,
            )
        if (burdeSynceContextMedGcp()) {
            contextHolderClient
                .oppdaterVeiledersContext(nyContext, veilederIdent)
                .getOrThrow()
        } else {
            if (EventType.NY_AKTIV_BRUKER.name == nyContext.eventType && nyContext.verdi.isEmpty()) {
                nullstillAktivBruker(veilederIdent)
                return
            } else if (nyContext.verdi.isEmpty()) {
                log.warn("Forsøk på å sette aktivEnhet til null, vil generere feil.")
            }

            saveToDb(event)
        }

        val message = JsonUtils.toJson(RSEvent.from(event))
        redisPublisher.publishMessage(message)
    }

    fun hentAktivBruker(veilederIdent: String): RSContext =
        if (burdeSynceContextMedGcp()) {
            contextHolderClient
                .hentAktivBruker(veilederIdent)
                .getOrThrow()
        } else {
            veilederContextDatabase
                .sistAktiveBrukerEvent(veilederIdent)
                ?.takeIf(::erFortsattAktuell)
                ?.let(RSContext::from)
                ?: RSContext()
        }

    fun hentAktivBrukerV2(veilederIdent: String): RSAktivBruker =
        if (burdeSynceContextMedGcp()) {
            contextHolderClient
                .hentAktivBrukerV2(veilederIdent)
                .getOrThrow()
        } else {
            veilederContextDatabase
                .sistAktiveBrukerEvent(veilederIdent)
                ?.takeIf(::erFortsattAktuell)
                ?.let(RSAktivBruker::from)
                ?: RSAktivBruker(null)
        }

    fun hentAktivEnhet(veilederIdent: String): RSContext =
        if (burdeSynceContextMedGcp()) {
            contextHolderClient
                .hentAktivEnhet(veilederIdent)
                .getOrThrow()
        } else {
            veilederContextDatabase
                .sistAktiveEnhetEvent(veilederIdent)
                ?.let(RSContext::from)
                ?: RSContext()
        }

    fun hentAktivEnhetV2(veilederIdent: String): RSAktivEnhet =
        if (burdeSynceContextMedGcp()) {
            contextHolderClient
                .hentAktivEnhetV2(veilederIdent)
                .getOrThrow()
        } else {
            veilederContextDatabase
                .sistAktiveEnhetEvent(veilederIdent)
                ?.let(RSAktivEnhet::from)
                ?: RSAktivEnhet(null)
        }

    fun nullstillContext(veilederIdent: String) {
        if (burdeSynceContextMedGcp()) {
            contextHolderClient.nullstillBrukerContext(veilederIdent)
        } else {
            veilederContextDatabase.slettAllEventer(veilederIdent)
        }
    }

    fun nullstillAktivBruker(veilederIdent: String) {
        if (burdeSynceContextMedGcp()) {
            contextHolderClient.nullstillAktivBruker(veilederIdent)
        } else {
            veilederContextDatabase.slettAlleAvEventTypeForVeileder(EventType.NY_AKTIV_BRUKER.name, veilederIdent)
        }
    }

    private fun saveToDb(event: PEvent): Long = veilederContextDatabase.save(event)

    private fun burdeSynceContextMedGcp(): Boolean =
        ApplicationCluster.isFss() && toggleableFeatureService.isEnabled(ToggleableFeatures.SYNC_CONTEXT_MED_GCP)
}
