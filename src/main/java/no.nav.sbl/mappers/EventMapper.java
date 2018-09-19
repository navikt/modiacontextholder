package no.nav.sbl.mappers;

import no.nav.sbl.db.domain.PEvent;
import no.nav.sbl.rest.domain.RSContext;
import no.nav.sbl.rest.domain.RSEvent;

import java.util.function.Function;

import static no.nav.sbl.db.domain.EventType.NY_AKTIV_BRUKER;
import static no.nav.sbl.db.domain.EventType.NY_AKTIV_ENHET;

public class EventMapper {

    public static Function<PEvent, RSContext> p2context = event -> new RSContext()
            .aktivEnhet(NY_AKTIV_ENHET.name().equals(event.eventType) ? event.verdi : null)
            .aktivBruker(NY_AKTIV_BRUKER.name().equals(event.eventType) ? event.verdi : null);

    public static Function<PEvent, RSEvent> p2event = event -> new RSEvent()
            .id(event.id)
            .eventType(event.eventType)
            .veilederIdent(event.veilederIdent);
}
