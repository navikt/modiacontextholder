package no.nav.sbl.service

import no.nav.common.json.JsonUtils
import no.nav.sbl.db.dao.EventDAO
import no.nav.sbl.db.domain.EventType
import no.nav.sbl.db.domain.PEvent
import no.nav.sbl.mappers.EventMapper
import no.nav.sbl.redis.RedisPublisher
import no.nav.sbl.rest.domain.RSAktivBruker
import no.nav.sbl.rest.domain.RSAktivEnhet
import no.nav.sbl.rest.domain.RSContext
import no.nav.sbl.rest.domain.RSNyContext
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class ContextService(
    private val eventDAO: EventDAO,
    private val redisPublisher: RedisPublisher
) {
    private val log = LoggerFactory.getLogger(ContextService::class.java)

    companion object {
        @JvmStatic
        fun erFortsattAktuell(pEvent: PEvent): Boolean {
            return LocalDate.now().isEqual(pEvent.created?.toLocalDate())
        }
    }

    fun hentVeiledersContext(veilederIdent: String): RSContext {
        return RSContext(
            hentAktivBruker(veilederIdent).aktivBruker,
            hentAktivEnhet(veilederIdent).aktivEnhet
        )
    }

    fun oppdaterVeiledersContext(nyContext: RSNyContext, veilederIdent: String) {
        if (EventType.NY_AKTIV_BRUKER.name == nyContext.eventType && nyContext.verdi.isNullOrEmpty()) {
            nullstillAktivBruker(veilederIdent)
            return
        } else if (nyContext.verdi.isNullOrEmpty()) {
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

    private fun saveToDb(event: PEvent): Long {
        return eventDAO.save(event)
    }

    fun hentAktivBruker(veilederIdent: String): RSContext {
        return eventDAO.sistAktiveBrukerEvent(veilederIdent)
            .filter(::erFortsattAktuell)
            .map(EventMapper::toRSContext)
            .orElse(RSContext())
    }

    fun hentAktivBrukerV2(veilederIdent: String): RSAktivBruker {
        return eventDAO.sistAktiveBrukerEvent(veilederIdent)
            .filter(::erFortsattAktuell)
            .map(EventMapper::toRSAktivBruker)
            .orElse(RSAktivBruker(null))
    }

    fun hentAktivEnhet(veilederIdent: String): RSContext {
        return eventDAO.sistAktiveEnhetEvent(veilederIdent)
            .map(EventMapper::toRSContext)
            .orElse(RSContext())
    }

    fun hentAktivEnhetV2(veilederIdent: String): RSAktivEnhet {
        return eventDAO.sistAktiveEnhetEvent(veilederIdent)
            .map(EventMapper::toRSAktivEnhet)
            .orElse(RSAktivEnhet(null))
    }

    fun nullstillContext(veilederIdent: String) {
        eventDAO.slettAllEventer(veilederIdent)
    }

    fun nullstillAktivBruker(veilederIdent: String) {
        eventDAO.slettAlleAvEventTypeForVeileder(EventType.NY_AKTIV_BRUKER.name, veilederIdent)
    }
}