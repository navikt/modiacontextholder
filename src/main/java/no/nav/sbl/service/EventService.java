package no.nav.sbl.service;

import no.nav.sbl.db.dao.EventDAO;
import no.nav.sbl.mappers.EventMapper;
import no.nav.sbl.rest.domain.RSEvent;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static java.util.stream.Collectors.toList;

public class EventService {

    @Autowired
    private EventDAO eventDAO;

    public List<RSEvent> hentEventerEtterId(long id) {
        return eventDAO.finnAlleEventerEtterId(id)
                .stream()
                .map(EventMapper::toRSEvent)
                .collect(toList());
    }
}
