package no.nav.sbl.service;

import no.nav.sbl.db.dao.EventDAO;
import no.nav.sbl.db.domain.PEvent;
import no.nav.sbl.mappers.EventMapper;
import no.nav.sbl.rest.domain.RSContext;
import no.nav.sbl.rest.domain.RSNyContext;

import javax.inject.Inject;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static no.nav.sbl.db.domain.EventType.NY_AKTIV_BRUKER;

public class ContextService {

    private final EventDAO eventDAO;

    @Inject
    public ContextService(EventDAO eventDAO) {
        this.eventDAO = eventDAO;
    }

    public RSContext hentVeiledersContext(String veilederIdent) {
        return new RSContext()
                .aktivBruker(hentAktivBruker(veilederIdent).aktivBruker)
                .aktivEnhet(hentAktivEnhet(veilederIdent).aktivEnhet);
    }

    public void oppdaterVeiledersContext(RSNyContext context, String veilederIdent) {
        eventDAO.save(new PEvent()
                .verdi(context.verdi)
                .eventType(context.eventType)
                .veilederIdent(veilederIdent));
    }

    public RSContext hentAktivBruker(String veilederIdent) {
        return eventDAO.sistAktiveBrukerEvent(veilederIdent)
                .filter(this::erFortsattAktuell)
                .map(EventMapper::toRSContext)
                .orElse(new RSContext());
    }

    private boolean erFortsattAktuell(PEvent pEvent) {
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
