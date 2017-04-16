package no.nav.sbl.service;

import no.nav.sbl.db.dao.EventDAO;
import no.nav.sbl.rest.domain.RSEvent;

import javax.inject.Inject;
import java.util.List;

import static no.nav.sbl.mappers.EventMapper.p2event;
import static no.nav.sbl.util.MapUtil.mapListe;

public class EventService {

    @Inject
    private EventDAO eventDAO;

    public List<RSEvent> hentEventerEtterId(long id) {
        return mapListe(eventDAO.finnAlleEventerEtterId(id), p2event);
    }
}
