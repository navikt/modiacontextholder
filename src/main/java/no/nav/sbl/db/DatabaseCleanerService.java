package no.nav.sbl.db;

import io.micrometer.core.annotation.Timed;
import no.nav.sbl.db.dao.EventDAO;
import no.nav.sbl.db.domain.PEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;


import java.util.List;

import static no.nav.sbl.db.domain.EventType.NY_AKTIV_BRUKER;
import static no.nav.sbl.db.domain.EventType.NY_AKTIV_ENHET;

public class DatabaseCleanerService {

    @Autowired
    private EventDAO eventDAO;

    @Scheduled(cron = "0 0 2 * * *")
    @Timed("slettAlleNyAktivBrukerEvents")
    public void slettAlleNyAktivBrukerEvents() {
        eventDAO.slettAlleAvEventType(NY_AKTIV_BRUKER.name());
    }

    @Scheduled(cron = "0 0 3 * * *")
    @Timed("slettAlleUtenomSisteNyAktivEnhet")
    public void slettAlleUtenomSisteNyAktivEnhet() {
        eventDAO.hentUnikeVeilederIdenter()
                .forEach(veilederIdent -> {
                    List<PEvent> eventer = eventDAO.hentVeiledersEventerAvType(NY_AKTIV_ENHET.name(), veilederIdent);
                    eventDAO.slettAlleEventerUtenomNyeste(eventer);
                });
    }
}
