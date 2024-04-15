package no.nav.sbl.service;

import lombok.extern.slf4j.Slf4j;
import no.nav.common.json.JsonUtils;
import no.nav.sbl.db.dao.EventDAO;
import no.nav.sbl.db.domain.PEvent;
import no.nav.sbl.mappers.EventMapper;
import no.nav.sbl.redis.RedisPublisher;
import no.nav.sbl.rest.domain.RSAktivBruker;
import no.nav.sbl.rest.domain.RSAktivEnhet;
import no.nav.sbl.rest.domain.RSContext;
import no.nav.sbl.rest.domain.RSNyContext;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;

import static no.nav.common.utils.StringUtils.nullOrEmpty;
import static no.nav.sbl.db.domain.EventType.NY_AKTIV_BRUKER;
import static no.nav.sbl.mappers.EventMapper.toRSEvent;

@Slf4j
public class ContextService {

    private final EventDAO eventDAO;

    private final RedisPublisher redisPublisher;

    @Autowired
    public ContextService(EventDAO eventDAO, RedisPublisher redisPublisher) {
        this.eventDAO = eventDAO;
        this.redisPublisher = redisPublisher;
    }

    public RSContext hentVeiledersContext(String veilederIdent) {
        return new RSContext()
                .aktivBruker(hentAktivBruker(veilederIdent).aktivBruker)
                .aktivEnhet(hentAktivEnhet(veilederIdent).aktivEnhet);
    }

    public void oppdaterVeiledersContext(RSNyContext nyContext, String veilederIdent) {
        if (NY_AKTIV_BRUKER.name().equals(nyContext.eventType) && nullOrEmpty(nyContext.verdi)) {
            nullstillAktivBruker(veilederIdent);
            return;
        } else if (nullOrEmpty(nyContext.verdi)) {
            log.warn("Forsøk på å sette aktivEnhet til null, vil generere feil.");
        }

        PEvent event = new PEvent()
                .verdi(nyContext.verdi)
                .eventType(nyContext.eventType)
                .veilederIdent(veilederIdent);

        long id = saveToDb(event);
        event = event.id(id);
        String message = JsonUtils.toJson(toRSEvent(event));
        redisPublisher.publishMessage(message);
    }

    private long saveToDb(PEvent event) {
        return eventDAO.save(event);
    }

    public RSContext hentAktivBruker(String veilederIdent) {
        return eventDAO.sistAktiveBrukerEvent(veilederIdent)
                .filter(ContextService::erFortsattAktuell)
                .map(EventMapper::toRSContext)
                .orElse(new RSContext());
    }

    public RSAktivBruker hentAktivBrukerV2(String veilederIdent) {
        return eventDAO.sistAktiveBrukerEvent(veilederIdent)
                .filter(ContextService::erFortsattAktuell)
                .map(EventMapper::toRSAktivBruker)
                .orElse(new RSAktivBruker(null));
    }


    public static boolean erFortsattAktuell(PEvent pEvent) {
        return LocalDate.now().isEqual(pEvent.created.toLocalDate());
    }

    public RSContext hentAktivEnhet(String veilederIdent) {
        return eventDAO.sistAktiveEnhetEvent(veilederIdent)
                .map(EventMapper::toRSContext)
                .orElse(new RSContext());
    }

    public RSAktivEnhet hentAktivEnhetV2(String veilederIdent) {
        return eventDAO.sistAktiveEnhetEvent(veilederIdent).map(EventMapper::toRSAktivEnhet).orElse(new RSAktivEnhet(null));
    }

    public void nullstillContext(String veilederIdent) {
        eventDAO.slettAllEventer(veilederIdent);
    }

    public void nullstillAktivBruker(String veilederIdent) {
        eventDAO.slettAlleAvEventTypeForVeileder(NY_AKTIV_BRUKER.name(), veilederIdent);
    }
}
