package no.nav.sbl.rest;

import no.nav.sbl.rest.domain.RSEvents;
import no.nav.sbl.service.EventService;
import org.springframework.stereotype.Controller;

import javax.inject.Inject;
import javax.ws.rs.*;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Controller
@Path("/events")
@Consumes(APPLICATION_JSON)
@Produces(APPLICATION_JSON)
public class EventRessurs {

    @Inject
    private EventService eventService;

    @GET
    @Path("{eventId}")
    public RSEvents hentNyeEvents(@PathParam("eventId") String eventId) {
        return new RSEvents().withEvents(eventService.hentEventerEtterId(Long.parseLong(eventId)));
    }
}
