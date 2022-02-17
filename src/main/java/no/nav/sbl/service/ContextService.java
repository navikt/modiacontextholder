package no.nav.sbl.service;

import lombok.extern.slf4j.Slf4j;
import no.nav.common.json.JsonUtils;
import no.nav.sbl.db.dao.EventDAO;
import no.nav.sbl.db.domain.PEvent;
import no.nav.sbl.mappers.EventMapper;
import no.nav.sbl.redis.Redis;
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

    private final Redis.Publisher redis;

    @Autowired
    public ContextService(EventDAO eventDAO, Redis.Publisher redis) {
        this.eventDAO = eventDAO;
        this.redis = redis;
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

        redis.publishMessage(Redis.getChannel(), JsonUtils.toJson(toRSEvent(event)));
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

    public static boolean erFortsattAktuell(PEvent pEvent) {
        return LocalDate.now().isEqual(pEvent.created.toLocalDate());
    }

    public RSContext hentAktivEnhet(String veilederIdent) {
        return eventDAO.sistAktiveEnhetEvent(veilederIdent)
                .map(EventMapper::toRSContext)
                .orElse(new RSContext());
    }

    public void nullstillContext(String veilederIdent) {
        eventDAO.slettAllEventer(veilederIdent);
    }

    public void nullstillAktivBruker(String veilederIdent) {
        eventDAO.slettAlleAvEventTypeForVeileder(NY_AKTIV_BRUKER.name(), veilederIdent);
    }
}
