package no.nav.sbl.service;


import no.nav.sbl.db.dao.EventDAO;
import no.nav.sbl.db.domain.PEvent;
import no.nav.sbl.rest.domain.RSContext;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.Optional;

import static java.time.LocalDateTime.now;
import static no.nav.sbl.db.domain.EventType.NY_AKTIV_BRUKER;
import static no.nav.sbl.service.ContextService.erFortsattAktuell;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ContextServiceTest {

    private static final String BRUKER_ID = "bruker";

    private EventDAO eventDAO;
    private ContextService contextService;

    @Before
    public void setup(){
        eventDAO = mock(EventDAO.class);
        contextService = new ContextService(eventDAO);
    }

    @Test
    public void ingen_aktiv_bruker_event(){
        har_ikke_aktiv_bruker();
    }

    @Test
    public void eventer_fra_forrige_dag_regnes_ikke_som_aktuelle() {
        PEvent event = new PEvent().created(now().minusDays(1));
        boolean result = erFortsattAktuell(event);
        assertThat(result).isFalse();
    }

    @Test
    public void eventer_fra_i_dag_regnes_som_aktuelle() {
        PEvent event = new PEvent().created(now());
        boolean result = erFortsattAktuell(event);
        assertThat(result).isTrue();
    }

    @Test
    public void aktiv_bruker_event(){
        LocalDateTime now = now();
        gitt_sist_aktive_bruker_event(now);
        assertThat(contextService.hentAktivBruker("ident")).isEqualTo(new RSContext().aktivBruker(BRUKER_ID));
    }

    @Test
    public void foreldet_aktiv_bruker_event(){
        gitt_sist_aktive_bruker_event(now().minusDays(2));
        har_ikke_aktiv_bruker();

        gitt_sist_aktive_bruker_event(now().minusDays(1).plusSeconds(1)); // i går men mindre enn en dag
        har_ikke_aktiv_bruker();
    }

    private void har_ikke_aktiv_bruker() {
        assertThat(contextService.hentAktivBruker("ident")).isEqualTo(new RSContext());
    }

    private void gitt_sist_aktive_bruker_event(LocalDateTime created) {
        PEvent pEvent = new PEvent().eventType(NY_AKTIV_BRUKER.name()).verdi(BRUKER_ID).created(created);
        when(eventDAO.sistAktiveBrukerEvent(anyString())).thenReturn(Optional.of(pEvent));
    }

}
