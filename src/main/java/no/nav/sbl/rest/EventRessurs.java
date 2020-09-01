package no.nav.sbl.rest;

import no.nav.sbl.rest.domain.RSEvents;
import no.nav.sbl.service.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/events")
public class EventRessurs {

    @Autowired
    private EventService eventService;

    @GetMapping("{eventId}")
    public RSEvents hentNyeEvents(@PathVariable("eventId") String eventId) {
        return new RSEvents().events(eventService.hentEventerEtterId(Long.parseLong(eventId)));
    }
}
