package no.nav.sbl.mappers;

import no.nav.sbl.db.domain.PEvent;
import no.nav.sbl.rest.domain.RSAktivBruker;
import no.nav.sbl.rest.domain.RSAktivEnhet;
import no.nav.sbl.rest.domain.RSContext;
import no.nav.sbl.rest.domain.RSEvent;

import java.util.function.Function;

import static no.nav.sbl.db.domain.EventType.NY_AKTIV_BRUKER;
import static no.nav.sbl.db.domain.EventType.NY_AKTIV_ENHET;

public class EventMapper {

    public static RSContext toRSContext(PEvent event) {
        return new RSContext(
                NY_AKTIV_BRUKER.name().equals(event.getEventType()) ? event.getVerdi() : null,
                NY_AKTIV_ENHET.name().equals(event.getEventType()) ? event.getVerdi() : null
        );
    }

    public static RSAktivBruker toRSAktivBruker(PEvent event) {
        return new RSAktivBruker(
                NY_AKTIV_BRUKER.name().equals(event.getEventType()) ? event.getVerdi() : null
        );
    }

    public static RSAktivEnhet toRSAktivEnhet(PEvent event) {
        return new RSAktivEnhet(NY_AKTIV_ENHET.name().equals(event.getEventType()) ? event.getVerdi() : null);
    }

    public static RSEvent toRSEvent(PEvent event) {
        return new RSEvent()
                .id(event.getId())
                .eventType(event.getEventType())
                .veilederIdent(event.getVeilederIdent());
    }

}
