package no.nav.sbl.db;

import no.nav.metrics.aspects.Timed;
import no.nav.sbl.db.dao.EventDAO;
import no.nav.sbl.db.domain.PEvent;
import no.nav.sbl.util.Utils;
import org.springframework.scheduling.annotation.Scheduled;

import javax.inject.Inject;

import java.util.List;

import static no.nav.sbl.db.domain.EventType.NY_AKTIV_BRUKER;
import static no.nav.sbl.db.domain.EventType.NY_AKTIV_ENHET;

public class DatabaseCleanerService {

    @Inject
    private EventDAO eventDAO;

    @Scheduled(cron = "0 0 2 * * *")
    @Timed(name = "slettAlleNyAktivBrukerEvents")
    public void slettAlleNyAktivBrukerEvents() {
        if(Utils.isMasterNode()) {
            eventDAO.slettAlleAvEventType(NY_AKTIV_BRUKER.name());

        }
    }

    @Scheduled(cron = "0 0 3 * * *")
    @Timed(name = "slettAlleUtenomSisteNyAktivEnhet")
    public void slettAlleUtenomSisteNyAktivEnhet() {
        if(Utils.isMasterNode()) {
            eventDAO.hentUnikeVeilederIdenter()
                    .forEach(veilederIdent -> {
                        List<PEvent> eventer = eventDAO.hentVeiledersEventerAvType(NY_AKTIV_ENHET.name(), veilederIdent);
                        eventDAO.slettAlleEventerUtenomNyeste(eventer);
                    });
        }
    }
}
