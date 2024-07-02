package no.nav.sbl.rest

import no.nav.sbl.rest.domain.RSEvents
import no.nav.sbl.service.EventService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(value = ["/api/events", "/modiacontextholder/api/events"])
class EventRessurs {
    @Autowired
    lateinit var eventService: EventService

    @GetMapping("{eventId}")
    fun hentNyeEvents(
        @PathVariable("eventId") eventId: String,
    ): RSEvents =
        RSEvents(
            events = eventService.hentEventerEtterId(eventId.toLong()),
        )
}
