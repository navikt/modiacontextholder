package no.nav.sbl.service

import no.nav.common.json.JsonUtils
import no.nav.sbl.config.ApplicationCluster
import no.nav.sbl.consumers.modiacontextholder.ModiaContextHolderClient
import no.nav.sbl.db.dao.EventDAO
import no.nav.sbl.db.domain.EventType
import no.nav.sbl.db.domain.PEvent
import no.nav.sbl.mappers.EventMapper
import no.nav.sbl.redis.RedisPublisher
import no.nav.sbl.rest.domain.RSAktivBruker
import no.nav.sbl.rest.domain.RSAktivEnhet
import no.nav.sbl.rest.domain.RSContext
import no.nav.sbl.rest.domain.RSNyContext
import no.nav.sbl.service.unleash.Feature
import no.nav.sbl.service.unleash.UnleashService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class ContextService(
    private val eventDAO: EventDAO,
    private val redisPublisher: RedisPublisher,
    private val contextHolderClient: ModiaContextHolderClient,
    private val unleashService: UnleashService,
    private val applicationCluster: ApplicationCluster,
) {
    private val log = LoggerFactory.getLogger(ContextService::class.java)

    companion object {
        @JvmStatic
        fun erFortsattAktuell(pEvent: PEvent): Boolean {
            return LocalDate.now().isEqual(pEvent.created?.toLocalDate())
        }
    }

    fun hentVeiledersContext(veilederIdent: String): RSContext {
        return if (burdeHenteContextFraGcp()) {
            contextHolderClient.hentVeiledersContext(veilederIdent)
                .getOrThrow()
        } else {
            RSContext(
                hentAktivBruker(veilederIdent).aktivBruker,
                hentAktivEnhet(veilederIdent).aktivEnhet
            )
        }
    }

    fun oppdaterVeiledersContext(nyContext: RSNyContext, veilederIdent: String) {
        if (burdeSendeContextTilGcp()) {
            contextHolderClient.oppdaterVeiledersContext(nyContext, veilederIdent)
        }
        if (EventType.NY_AKTIV_BRUKER.name == nyContext.eventType && nyContext.verdi.isEmpty()) {
            nullstillAktivBruker(veilederIdent)
            return
        } else if (nyContext.verdi.isEmpty()) {
            log.warn("Forsøk på å sette aktivEnhet til null, vil generere feil.")
        }

        val event = PEvent(
            verdi = nyContext.verdi,
            eventType = nyContext.eventType,
            veilederIdent = veilederIdent
        )

        val id = saveToDb(event)
        val message = JsonUtils.toJson(EventMapper.toRSEvent(event.copy(id = id)))
        redisPublisher.publishMessage(message)
    }

    fun hentAktivBruker(veilederIdent: String): RSContext {
        return if (burdeHenteContextFraGcp()) {
            contextHolderClient.hentAktivBruker(veilederIdent)
                .getOrThrow()
        } else {
            eventDAO.sistAktiveBrukerEvent(veilederIdent)
                ?.takeIf(::erFortsattAktuell)
                ?.let(EventMapper::toRSContext)
                ?: RSContext()
        }
    }

    fun hentAktivBrukerV2(veilederIdent: String): RSAktivBruker {
        return if (burdeHenteContextFraGcp()) {
            contextHolderClient.hentAktivBrukerV2(veilederIdent)
                .getOrThrow()
        } else {
            eventDAO.sistAktiveBrukerEvent(veilederIdent)
                ?.takeIf(::erFortsattAktuell)
                ?.let(EventMapper::toRSAktivBruker)
                ?: RSAktivBruker(null)
        }
    }

    fun hentAktivEnhet(veilederIdent: String): RSContext {
        return if (burdeHenteContextFraGcp()) {
            contextHolderClient.hentAktivEnhet(veilederIdent)
                .getOrThrow()
        } else {
            eventDAO.sistAktiveEnhetEvent(veilederIdent)
                ?.let(EventMapper::toRSContext)
                ?: RSContext()
        }
    }

    fun hentAktivEnhetV2(veilederIdent: String): RSAktivEnhet {
        return if (burdeHenteContextFraGcp()) {
            contextHolderClient.hentAktivEnhetV2(veilederIdent)
                .getOrThrow()
        } else {
            eventDAO.sistAktiveEnhetEvent(veilederIdent)
                ?.let(EventMapper::toRSAktivEnhet)
                ?: RSAktivEnhet(null)
        }
    }

    fun nullstillContext(veilederIdent: String) {
        if (burdeSendeContextTilGcp()) {
            contextHolderClient.nullstillContext(veilederIdent)
        }
        eventDAO.slettAllEventer(veilederIdent)
    }

    fun nullstillAktivBruker(veilederIdent: String) {
        if (burdeSendeContextTilGcp()) {
            contextHolderClient.nullstillAktivBruker(veilederIdent)
        }
        eventDAO.slettAlleAvEventTypeForVeileder(EventType.NY_AKTIV_BRUKER.name, veilederIdent)
    }

    private fun saveToDb(event: PEvent): Long {
        return eventDAO.save(event)
    }

    private fun burdeHenteContextFraGcp(): Boolean =
        applicationCluster.isFss() && unleashService.isEnabled(Feature.HENT_CONTEXT_FRA_GCP)

    private fun burdeSendeContextTilGcp(): Boolean =
        applicationCluster.isFss() && unleashService.isEnabled(Feature.SEND_CONTEXT_TIL_GCP)
}