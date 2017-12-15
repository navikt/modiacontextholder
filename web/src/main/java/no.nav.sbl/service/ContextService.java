package no.nav.sbl.service;

import no.nav.sbl.db.dao.EventDAO;
import no.nav.sbl.db.domain.PEvent;
import no.nav.sbl.rest.domain.RSContext;
import no.nav.sbl.rest.domain.RSNyContext;

import javax.inject.Inject;

import static no.nav.sbl.mappers.EventMapper.p2context;
import static no.nav.sbl.util.MapUtil.map;

public class ContextService {

    @Inject
    private EventDAO eventDAO;

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
        PEvent sisteAktivBrukerEvent = eventDAO.sistAktiveBrukerEvent(veilederIdent).orElse(new PEvent());
        return map(sisteAktivBrukerEvent, p2context);
    }

    public RSContext hentAktivEnhet(String veilederIdent) {
        PEvent sisteAktivEnhetEvent = eventDAO.sistAktiveEnhetEvent(veilederIdent).orElse(new PEvent());
        return map(sisteAktivEnhetEvent, p2context);
    }

    public void nullstillContext(String veilederIdent) {
        eventDAO.slettAllEventer(veilederIdent);
    }
}
