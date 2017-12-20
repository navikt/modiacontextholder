package no.nav.sbl.service;

import no.nav.sbl.db.dao.EventDAO;
import no.nav.sbl.db.domain.EventType;
import no.nav.sbl.db.domain.PEvent;
import no.nav.sbl.mappers.EventMapper;
import no.nav.sbl.rest.domain.RSContext;
import no.nav.sbl.rest.domain.RSNyContext;
import no.nav.sbl.util.MapUtil;

import javax.inject.Inject;
import javax.ws.rs.NotFoundException;

import java.util.Optional;

import static no.nav.sbl.db.domain.EventType.NY_AKTIV_BRUKER;
import static no.nav.sbl.mappers.EventMapper.*;
import static no.nav.sbl.util.MapUtil.map;

public class ContextService {

    @Inject
    private EventDAO eventDAO;

    public RSContext hentVeiledersContext(String veilederIdent, String remoteAdress) {
        return new RSContext()
                .withAktivBruker(hentAktivBruker(veilederIdent, remoteAdress).aktivBruker)
                .withAktivEnhet(hentAktivEnhet(veilederIdent).aktivEnhet);
    }

    public void oppdaterVeiledersContext(RSNyContext context, String veilederIdent) {
        eventDAO.save(new PEvent()
                .withVerdi(context.verdi)
                .withEventType(context.eventType)
                .withIp(context.ip)
                .withVeilederIdent(veilederIdent));
    }

    public RSContext hentAktivBruker(String veilederIdent, String remoteAdress) {
        PEvent sisteAktivBrukerEvent = eventDAO.sistAktiveBrukerEvent(veilederIdent, remoteAdress).orElse(new PEvent());
        return map(sisteAktivBrukerEvent, p2context);
    }

    public RSContext hentAktivEnhet(String veilederIdent) {
        PEvent sisteAktivEnhetEvent = eventDAO.sistAktiveEnhetEvent(veilederIdent).orElse(new PEvent());
        return map(sisteAktivEnhetEvent, p2context);
    }

    public void nullstillContext(String veilederIdent) {
        eventDAO.slettAllEventer(veilederIdent);
    }

    public void nullstillAktivBruker(String veilederIdent) {
        eventDAO.slettAlleAvEventTypeForVeileder(NY_AKTIV_BRUKER.name(), veilederIdent);
    }
}
